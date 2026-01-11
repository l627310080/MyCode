<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input
          v-model="queryParams.ruleName"
          placeholder="请输入规则名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="校验对象" prop="targetObject">
        <el-select v-model="queryParams.targetObject" placeholder="请选择校验对象" clearable>
          <el-option label="SPU (商品)" value="SPU" />
          <el-option label="SKU (规格)" value="SKU" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态" prop="isActive">
        <el-select v-model="queryParams.isActive" placeholder="请选择启用状态" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
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
          v-hasPermi="['system:rule-config:add']"
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
          v-hasPermi="['system:rule-config:edit']"
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
          v-hasPermi="['system:rule-config:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:rule-config:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="ruleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则ID" align="center" prop="ruleId" width="80" />
      <el-table-column label="规则名称" align="center" prop="ruleName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="校验对象" align="center" prop="targetObject" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.targetObject === 'SPU'" type="primary">SPU</el-tag>
          <el-tag v-else type="warning">SKU</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="校验字段" align="center" prop="targetField" width="120" />
      <el-table-column label="AI提示词" align="center" prop="aiPrompt" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="启用状态" align="center" prop="isActive" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isActive === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:rule-config:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:rule-config:remove']"
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

    <!-- 添加或修改规则对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="校验对象" prop="targetObject">
          <el-select v-model="form.targetObject" placeholder="请选择校验对象">
            <el-option label="SPU (商品)" value="SPU" />
            <el-option label="SKU (规格)" value="SKU" />
          </el-select>
        </el-form-item>
        <el-form-item label="校验字段" prop="targetField">
          <el-input v-model="form.targetField" placeholder="例如: productName, mainImage" />
        </el-form-item>
        <el-form-item label="AI提示词" prop="aiPrompt">
          <el-input v-model="form.aiPrompt" type="textarea" :rows="4" placeholder="请输入给AI的指令，例如：请检查是否包含违禁品..." />
        </el-form-item>
        <el-form-item label="失败提示" prop="errorMessage">
          <el-input v-model="form.errorMessage" placeholder="校验失败时显示的错误信息" />
        </el-form-item>
        <el-form-item label="启用状态" prop="isActive">
          <el-radio-group v-model="form.isActive">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { listRuleConfig, getRuleConfig, delRuleConfig, addRuleConfig, updateRuleConfig } from "@/api/system/ruleConfig"

export default {
  name: "RuleConfig",
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
      // 规则表格数据
      ruleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ruleName: null,
        targetObject: null,
        isActive: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        ruleName: [
          { required: true, message: "规则名称不能为空", trigger: "blur" }
        ],
        targetObject: [
          { required: true, message: "校验对象不能为空", trigger: "change" }
        ],
        targetField: [
          { required: true, message: "校验字段不能为空", trigger: "blur" }
        ],
        aiPrompt: [
          { required: true, message: "AI提示词不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询规则列表 */
    getList() {
      this.loading = true
      listRuleConfig(this.queryParams).then(response => {
        this.ruleList = response.rows
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
        ruleId: null,
        ruleName: null,
        targetObject: null,
        targetField: null,
        aiPrompt: null,
        errorMessage: null,
        isActive: 1,
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
      this.ids = selection.map(item => item.ruleId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加校验规则"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const ruleId = row.ruleId || this.ids
      getRuleConfig(ruleId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改校验规则"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.ruleId != null) {
            updateRuleConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addRuleConfig(this.form).then(response => {
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
      const ruleIds = row.ruleId || this.ids
      this.$modal.confirm('是否确认删除规则编号为"' + ruleIds + '"的数据项？').then(function() {
        return delRuleConfig(ruleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/rule-config/export', {
        ...this.queryParams
      }, `rule_config_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
