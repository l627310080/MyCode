<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="关联的SPU主键ID" prop="spuId">
        <el-input
          v-model="queryParams.spuId"
          clearable
          placeholder="请输入关联的SPU主键ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="内部SKU编码，对应实物条码" prop="skuCode">
        <el-input
          v-model="queryParams.skuCode"
          clearable
          placeholder="请输入内部SKU编码，对应实物条码"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当前采购成本价(人民币)" prop="purchasePrice">
        <el-input
          v-model="queryParams.purchasePrice"
          clearable
          placeholder="请输入当前采购成本价(人民币)"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="虚拟仓储可用库存总量" prop="stockQty">
        <el-input
          v-model="queryParams.stockQty"
          clearable
          placeholder="请输入虚拟仓储可用库存总量"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品单件重量，单位:KG" prop="weightKg">
        <el-input
          v-model="queryParams.weightKg"
          clearable
          placeholder="请输入商品单件重量，单位:KG"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:sku:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:sku:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:sku:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:sku:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="skuList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="关联的SPU主键ID" prop="spuId"/>
      <el-table-column align="center" label="内部SKU编码，对应实物条码" prop="skuCode"/>
      <el-table-column align="center" label="规格详情描述" prop="specInfo"/>
      <el-table-column align="center" label="SKU规格图" prop="skuImage" width="100">
        <template slot-scope="scope">
          <image-preview :height="50" :src="scope.row.skuImage" :width="50"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="当前采购成本价(人民币)" prop="purchasePrice"/>
      <el-table-column align="center" label="虚拟仓储可用库存总量" prop="stockQty"/>
      <el-table-column align="center" label="商品单件重量，单位:KG" prop="weightKg"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['system:sku:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['system:sku:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改商品单品规格(SKU)对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联的SPU主键ID" prop="spuId">
          <el-input v-model="form.spuId" placeholder="请输入关联的SPU主键ID"/>
        </el-form-item>
        <el-form-item label="内部SKU编码，对应实物条码" prop="skuCode">
          <el-input v-model="form.skuCode" placeholder="请输入内部SKU编码，对应实物条码"/>
        </el-form-item>
        <el-form-item label="规格详情描述" prop="specInfo">
          <el-input v-model="form.specInfo" placeholder="请输入内容" type="textarea"/>
        </el-form-item>
        <el-form-item label="SKU规格图" prop="skuImage">
          <image-upload v-model="form.skuImage"/>
        </el-form-item>
        <el-form-item label="当前采购成本价(人民币)" prop="purchasePrice">
          <el-input v-model="form.purchasePrice" placeholder="请输入当前采购成本价(人民币)"/>
        </el-form-item>
        <el-form-item label="虚拟仓储可用库存总量" prop="stockQty">
          <el-input v-model="form.stockQty" placeholder="请输入虚拟仓储可用库存总量"/>
        </el-form-item>
        <el-form-item label="商品单件重量，单位:KG" prop="weightKg">
          <el-input v-model="form.weightKg" placeholder="请输入商品单件重量，单位:KG"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addSku, delSku, getSku, listSku, updateSku} from "@/api/system/sku"

export default {
  name: "Sku",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 商品单品规格(SKU)表格数据
      skuList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        spuId: null,
        skuCode: null,
        specInfo: null,
        skuImage: null,
        purchasePrice: null,
        stockQty: null,
        weightKg: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        spuId: [
          {required: true, message: "关联的SPU主键ID不能为空", trigger: "blur"}
        ],
        skuCode: [
          {required: true, message: "内部SKU编码，对应实物条码不能为空", trigger: "blur"}
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询商品单品规格(SKU)列表 */
    getList() {
      this.loading = true
      listSku(this.queryParams).then(response => {
        this.skuList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        spuId: null,
        skuCode: null,
        specInfo: null,
        skuImage: null,
        purchasePrice: null,
        stockQty: null,
        weightKg: null,
        createTime: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加商品单品规格(SKU)"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getSku(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改商品单品规格(SKU)"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateSku(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addSku(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除商品单品规格(SKU)编号为"' + ids + '"的数据项？').then(function () {
        return delSku(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/sku/export', {
        ...this.queryParams
      }, `sku_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
