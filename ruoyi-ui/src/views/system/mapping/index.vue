<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="SKU ID" prop="skuId">
        <el-input
          v-model="queryParams.skuId"
          placeholder="请输入SKU ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="平台类型" prop="platformType">
        <el-input
          v-model="queryParams.platformType"
          placeholder="请输入平台类型"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="目标国家" prop="targetCountry">
        <el-input
          v-model="queryParams.targetCountry"
          placeholder="请输入目标国家"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="平台SKU" prop="platformSku">
        <el-input
          v-model="queryParams.platformSku"
          placeholder="请输入平台SKU"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="售价(USD)" prop="salePriceUsd">
        <el-input
          v-model="queryParams.salePriceUsd"
          placeholder="请输入售价"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="同步状态" prop="syncStatus">
        <el-input
          v-model="queryParams.syncStatus"
          placeholder="请输入同步状态"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:mapping:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:mapping:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:mapping:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:mapping:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="mappingList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" min-width="80" />
      <el-table-column label="SKU ID" align="center" prop="skuId" min-width="100" />
      <el-table-column label="平台类型" align="center" prop="platformType" min-width="120" />
      <el-table-column label="目标国家" align="center" prop="targetCountry" min-width="100" />
      <el-table-column label="平台SKU" align="center" prop="platformSku" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="售价(USD)" align="center" prop="salePriceUsd" min-width="120" />
      <el-table-column label="同步状态" align="center" prop="syncStatus" min-width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" min-width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-upload2"
            @click="handlePush(scope.row)"
            v-hasPermi="['system:mapping:edit']"
          >推送</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:mapping:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:mapping:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改跨平台商品映射对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="SKU ID" prop="skuId">
          <el-input v-model="form.skuId" placeholder="请输入SKU ID" />
        </el-form-item>
        <el-form-item label="平台类型" prop="platformType">
          <el-select v-model="form.platformType" placeholder="请选择平台">
            <el-option label="Amazon" value="AMAZON" />
            <el-option label="Shopee" value="SHOPEE" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标国家" prop="targetCountry">
          <el-select v-model="form.targetCountry" placeholder="请选择国家">
            <el-option label="美国 (US)" value="US" />
            <el-option label="英国 (UK)" value="UK" />
            <el-option label="日本 (JP)" value="JP" />
          </el-select>
        </el-form-item>
        <el-form-item label="平台SKU" prop="platformSku">
          <el-input v-model="form.platformSku" placeholder="请输入平台SKU" />
        </el-form-item>
        <el-form-item label="售价(USD)" prop="salePriceUsd">
          <el-input v-model="form.salePriceUsd" placeholder="请输入售价" />
        </el-form-item>
        <el-form-item label="同步状态" prop="syncStatus">
          <el-input v-model="form.syncStatus" placeholder="请输入同步状态" />
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
import { listMapping, getMapping, delMapping, addMapping, updateMapping } from "@/api/system/mapping"
import request from '@/utils/request'

export default {
  name: "Mapping",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      mappingList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        skuId: null,
        platformType: null,
        targetCountry: null,
        platformSku: null,
        salePriceUsd: null,
        syncStatus: null,
      },
      form: {},
      rules: {
        skuId: [
          { required: true, message: "SKU ID不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMapping(this.queryParams).then(response => {
        this.mappingList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: null,
        skuId: null,
        platformType: null,
        targetCountry: null,
        platformSku: null,
        salePriceUsd: null,
        syncStatus: null,
        createTime: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加跨平台商品映射"
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getMapping(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改跨平台商品映射"
      })
    },
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
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除跨平台商品映射编号为"' + ids + '"的数据项？').then(function() {
        return delMapping(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('system/mapping/export', {
        ...this.queryParams
      }, `mapping_${new Date().getTime()}.xlsx`)
    },
    handlePush(row) {
      const id = row.id;
      this.$modal.confirm('确认要推送该商品到 ' + row.platformType + ' (' + row.targetCountry + ') 吗？').then(function() {
        return request({
          url: '/system/mapping/push/' + id,
          method: 'post'
        })
      }).then(() => {
        this.$modal.msgSuccess("推送任务已提交");
      }).catch(() => {});
    }
  }
}
</script>
