<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="关联的本地SKU主键ID" prop="skuId">
        <el-input
          v-model="queryParams.skuId"
          clearable
          placeholder="请输入关联的本地SKU主键ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="该商品在对应平台上的刊登SKU编码" prop="platformSku">
        <el-input
          v-model="queryParams.platformSku"
          clearable
          placeholder="请输入该商品在对应平台上的刊登SKU编码"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="该平台的外币销售价格(美元)" prop="salePriceUsd">
        <el-input
          v-model="queryParams.salePriceUsd"
          clearable
          placeholder="请输入该平台的外币销售价格(美元)"
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
          v-hasPermi="['system:mapping:add']"
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
          v-hasPermi="['system:mapping:edit']"
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
          v-hasPermi="['system:mapping:remove']"
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
          v-hasPermi="['system:mapping:export']"
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

    <el-table v-loading="loading" :data="mappingList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="关联的本地SKU主键ID" prop="skuId"/>
      <el-table-column align="center" label="目标平台标识，如：AMAZON, SHOPEE, TIKTOK" prop="platformType"/>
      <el-table-column align="center" label="该商品在对应平台上的刊登SKU编码" prop="platformSku"/>
      <el-table-column align="center" label="该平台的外币销售价格(美元)" prop="salePriceUsd"/>
      <el-table-column align="center" label="平台同步状态：1-已同步，0-需重新推送" prop="syncStatus"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['system:mapping:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['system:mapping:remove']"
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

    <!-- 添加或修改跨平台商品映射对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联的本地SKU主键ID" prop="skuId">
          <el-input v-model="form.skuId" placeholder="请输入关联的本地SKU主键ID"/>
        </el-form-item>
        <el-form-item label="该商品在对应平台上的刊登SKU编码" prop="platformSku">
          <el-input v-model="form.platformSku" placeholder="请输入该商品在对应平台上的刊登SKU编码"/>
        </el-form-item>
        <el-form-item label="该平台的外币销售价格(美元)" prop="salePriceUsd">
          <el-input v-model="form.salePriceUsd" placeholder="请输入该平台的外币销售价格(美元)"/>
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
import {addMapping, delMapping, getMapping, listMapping, updateMapping} from "@/api/system/mapping"

export default {
  name: "Mapping",
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
      // 跨平台商品映射表格数据
      mappingList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        skuId: null,
        platformType: null,
        platformSku: null,
        salePriceUsd: null,
        syncStatus: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        skuId: [
          {required: true, message: "关联的本地SKU主键ID不能为空", trigger: "blur"}
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询跨平台商品映射列表 */
    getList() {
      this.loading = true
      listMapping(this.queryParams).then(response => {
        this.mappingList = response.rows
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
        skuId: null,
        platformType: null,
        platformSku: null,
        salePriceUsd: null,
        syncStatus: null,
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
      this.title = "添加跨平台商品映射"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getMapping(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改跨平台商品映射"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMapping(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addMapping(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除跨平台商品映射编号为"' + ids + '"的数据项？').then(function () {
        return delMapping(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/mapping/export', {
        ...this.queryParams
      }, `mapping_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
