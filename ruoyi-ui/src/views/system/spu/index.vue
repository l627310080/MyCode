<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="内部唯一SPU编码，用于仓库管理" prop="spuCode">
        <el-input
          v-model="queryParams.spuCode"
          clearable
          placeholder="请输入内部唯一SPU编码，用于仓库管理"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品标准标题，用于多语言翻译基础" prop="productName">
        <el-input
          v-model="queryParams.productName"
          clearable
          placeholder="请输入商品标准标题，用于多语言翻译基础"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="系统内部类目ID" prop="categoryId">
        <el-input
          v-model="queryParams.categoryId"
          clearable
          placeholder="请输入系统内部类目ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核状态：0-待审核，1-审核通过，2-合规拦截" prop="isAudit">
        <el-input
          v-model="queryParams.isAudit"
          clearable
          placeholder="请输入审核状态：0-待审核，1-审核通过，2-合规拦截"
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
          v-hasPermi="['system:spu:add']"
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
          v-hasPermi="['system:spu:edit']"
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
          v-hasPermi="['system:spu:remove']"
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
          v-hasPermi="['system:spu:export']"
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

    <el-table v-loading="loading" :data="spuList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="内部唯一SPU编码，用于仓库管理" prop="spuCode"/>
      <el-table-column align="center" label="商品标准标题，用于多语言翻译基础" prop="productName"/>
      <el-table-column align="center" label="系统内部类目ID" prop="categoryId"/>
      <el-table-column align="center" label="商品展示主图网络地址" prop="mainImage" width="100">
        <template slot-scope="scope">
          <image-preview :height="50" :src="scope.row.mainImage" :width="50"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="审核状态" prop="isAudit">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isAudit === 0" type="info">待审核</el-tag>
          <el-tag v-else-if="scope.row.isAudit === 1" type="success">审核通过</el-tag>
          <el-tag v-else-if="scope.row.isAudit === 2" type="danger">合规拦截</el-tag>
          <span v-else>{{ scope.row.isAudit }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="业务备注说明" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['system:spu:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['system:spu:remove']"
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

    <!-- 添加或修改跨境商品标准信息(SPU)对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="内部唯一SPU编码，用于仓库管理" prop="spuCode">
          <el-input v-model="form.spuCode" placeholder="请输入内部唯一SPU编码，用于仓库管理"/>
        </el-form-item>
        <el-form-item label="商品标准标题，用于多语言翻译基础" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品标准标题，用于多语言翻译基础"/>
        </el-form-item>
        <el-form-item label="系统内部类目ID" prop="categoryId">
          <el-input v-model="form.categoryId" placeholder="请输入系统内部类目ID"/>
        </el-form-item>
        <el-form-item label="商品展示主图网络地址" prop="mainImage">
          <image-upload v-model="form.mainImage"/>
        </el-form-item>
        <el-form-item label="业务备注说明" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"/>
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
import {addSpu, delSpu, getSpu, listSpu, updateSpu} from "@/api/system/spu"

export default {
  name: "Spu",
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
      // 跨境商品标准信息(SPU)表格数据
      spuList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        spuCode: null,
        productName: null,
        categoryId: null,
        mainImage: null,
        isAudit: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        spuCode: [
          {required: true, message: "内部唯一SPU编码，用于仓库管理不能为空", trigger: "blur"}
        ],
        productName: [
          {required: true, message: "商品标准标题，用于多语言翻译基础不能为空", trigger: "blur"}
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询跨境商品标准信息(SPU)列表 */
    getList() {
      // 注意：这里不设置 this.loading = true，实现静默刷新
      // 只有第一次加载时 loading 默认为 true
      listSpu(this.queryParams).then(response => {
        this.spuList = response.rows
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
        spuCode: null,
        productName: null,
        categoryId: null,
        mainImage: null,
        isAudit: null,
        createBy: null,
        createTime: null,
        updateTime: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.loading = true // 手动搜索时显示 loading
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
      this.title = "添加跨境商品标准信息(SPU)"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getSpu(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改跨境商品标准信息(SPU)"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateSpu(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.loading = true
              this.getList()
            })
          } else {
            addSpu(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.loading = true
              this.getList() // 立即刷新一次

              // 轮询机制：每隔 1.5 秒刷新一次，共刷新 4 次
              // 这样可以覆盖 6 秒内的异步处理时间
              let count = 0;
              const interval = setInterval(() => {
                count++;
                this.getList(); // 静默刷新
                if (count >= 4) {
                  clearInterval(interval);
                }
              }, 1500);
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除跨境商品标准信息(SPU)编号为"' + ids + '"的数据项？').then(function () {
        return delSpu(ids)
      }).then(() => {
        this.loading = true
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/spu/export', {
        ...this.queryParams
      }, `spu_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
