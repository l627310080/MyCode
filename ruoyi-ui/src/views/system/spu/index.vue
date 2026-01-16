<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="SPU编码" prop="spuCode">
        <el-input
          v-model="queryParams.spuCode"
          placeholder="请输入SPU编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品标题" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入商品标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核状态" prop="isAudit">
        <el-input
          v-model="queryParams.isAudit"
          placeholder="请输入审核状态"
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
          v-hasPermi="['system:spu:add']"
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
          v-hasPermi="['system:spu:edit']"
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
          v-hasPermi="['system:spu:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:spu:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="spuList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" width="80" />
      <el-table-column label="SPU编码" align="center" prop="spuCode" width="150" />
      <el-table-column label="商品标题" align="center" prop="productName" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="类目ID" align="center" prop="categoryId" width="100" />
      <el-table-column label="主图" align="center" prop="mainImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.mainImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="isAudit" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isAudit === 0" type="info">待审核</el-tag>
          <el-tag v-else-if="scope.row.isAudit === 1" type="success">审核通过</el-tag>
          <el-tag v-else-if="scope.row.isAudit === 2" type="danger">合规拦截</el-tag>
          <span v-else>{{ scope.row.isAudit }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:spu:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:spu:remove']"
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

    <!-- 添加或修改跨境商品标准信息(SPU)对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品类目" prop="categoryId">
          <treeselect
            v-model="form.categoryId"
            :options="categoryOptions"
            :normalizer="normalizer"
            :disable-branch-nodes="true"
            :show-count="true"
            placeholder="请选择商品类目"
          />
        </el-form-item>
        <el-form-item label="商品标题" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品标题" />
        </el-form-item>
        <el-form-item label="主图" prop="mainImage">
          <image-upload v-model="form.mainImage"/>
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
import { listSpu, getSpu, delSpu, addSpu, updateSpu } from "@/api/system/spu"
import { listCategory } from "@/api/system/category"; // 关键修改：改用 listCategory
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Spu",
  components: { Treeselect },
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
      // SPU表格数据
      spuList: [],
      // 类目树选项
      categoryOptions: [],
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
        isAudit: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        categoryId: [
          { required: true, message: "商品类目不能为空", trigger: "change" }
        ],
        productName: [
          { required: true, message: "商品标题不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询SPU列表 */
    getList() {
      listSpu(this.queryParams).then(response => {
        this.spuList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询类目下拉树结构 */
    getTreeselect() {
      // 关键修改：使用 listCategory + handleTree，复刻 Category 页面的逻辑
      listCategory().then(response => {
        this.categoryOptions = this.handleTree(response.data, "categoryId");
      });
    },
    /** 转换类目数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.categoryId,
        label: node.categoryName,
        children: node.children
      };
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
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.loading = true
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.getTreeselect();
      this.open = true
      this.title = "添加商品SPU"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      // 保证时序：先加载树，再加载详情
      listCategory().then(response => {
        this.categoryOptions = this.handleTree(response.data, "categoryId");

        const id = row.id || this.ids
        getSpu(id).then(response => {
          this.form = response.data
          this.open = true
          this.title = "修改商品SPU"
        })
      });
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
              this.getList()
              let count = 0;
              const interval = setInterval(() => {
                count++;
                this.getList();
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
      this.$modal.confirm('是否确认删除SPU编号为"' + ids + '"的数据项？').then(function() {
        return delSpu(ids)
      }).then(() => {
        this.loading = true
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
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
