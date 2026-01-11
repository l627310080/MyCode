<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="SPU ID" prop="spuId">
        <el-input
          v-model="queryParams.spuId"
          placeholder="请输入SPU ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="SKU编码" prop="skuCode">
        <el-input
          v-model="queryParams.skuCode"
          placeholder="请输入SKU编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="采购价" prop="purchasePrice">
        <el-input
          v-model="queryParams.purchasePrice"
          placeholder="请输入采购价"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="库存" prop="stockQty">
        <el-input
          v-model="queryParams.stockQty"
          placeholder="请输入库存"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="重量(kg)" prop="weightKg">
        <el-input
          v-model="queryParams.weightKg"
          placeholder="请输入重量"
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
          v-hasPermi="['system:sku:add']"
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
          v-hasPermi="['system:sku:edit']"
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
          v-hasPermi="['system:sku:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:sku:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="skuList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" width="80" />
      <el-table-column label="SPU ID" align="center" prop="spuId" width="100" />
      <el-table-column label="SKU编码" align="center" prop="skuCode" width="150" />
      <el-table-column label="规格描述" align="center" prop="specInfo" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="规格图" align="center" prop="skuImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.skuImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="采购价" align="center" prop="purchasePrice" width="100" />
      <el-table-column label="库存" align="center" prop="stockQty" width="100" />
      <el-table-column label="重量(kg)" align="center" prop="weightKg" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:sku:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:sku:remove']"
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

    <!-- 添加或修改商品单品规格(SKU)对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属SPU" prop="spuId">
              <el-select
                v-model="form.spuId"
                filterable
                remote
                reserve-keyword
                placeholder="请输入SPU名称搜索"
                :remote-method="remoteMethod"
                :loading="searchLoading">
                <el-option
                  v-for="item in spuOptions"
                  :key="item.id"
                  :label="item.productName"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SKU编码" prop="skuCode">
              <el-input v-model="form.skuCode" placeholder="请输入SKU编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格描述" prop="specInfo">
              <el-input v-model="form.specInfo" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格图" prop="skuImage">
              <image-upload v-model="form.skuImage"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购价" prop="purchasePrice">
              <el-input v-model="form.purchasePrice" placeholder="请输入采购价" @input="calculatePrices" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stockQty">
              <el-input v-model="form.stockQty" placeholder="请输入库存" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重量(kg)" prop="weightKg">
              <el-input v-model="form.weightKg" placeholder="请输入重量" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 一键铺货区域 -->
        <div v-if="!form.id">
          <el-divider content-position="left">一键铺货 (自动创建映射)</el-divider>
          <el-button type="primary" icon="el-icon-plus" size="mini" @click="addTargetMarket" style="margin-bottom: 10px;">添加发布目标</el-button>
          <el-table :data="form.targetMarkets" border style="width: 100%">
            <el-table-column label="平台" width="150">
              <template slot-scope="scope">
                <el-select v-model="scope.row.platform" placeholder="选择平台">
                  <el-option label="Amazon" value="AMAZON" />
                  <el-option label="Shopee" value="SHOPEE" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="国家" width="150">
              <template slot-scope="scope">
                <el-select v-model="scope.row.country" placeholder="选择国家" @change="handleCountryChange(scope.row)">
                  <el-option label="美国 (US)" value="US" />
                  <el-option label="英国 (UK)" value="UK" />
                  <el-option label="泰国 (TH)" value="TH" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="定价模式" width="150">
              <template slot-scope="scope">
                <el-select v-model="scope.row.priceMode" placeholder="模式" @change="calculateRowPrice(scope.row)">
                  <el-option label="固定价格" value="FIXED" />
                  <el-option label="倍数定价" value="MULTIPLIER" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数值 (价/倍)" width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.inputValue" placeholder="价格或倍数" @input="calculateRowPrice(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column label="最终售价 (USD)" width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.price" placeholder="自动计算" :disabled="scope.row.priceMode === 'MULTIPLIER'" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
              <template slot-scope="scope">
                <el-button type="text" icon="el-icon-delete" @click="removeTargetMarket(scope.$index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSku, getSku, delSku, addSku, updateSku } from "@/api/system/sku"
import { listSpu } from "@/api/system/spu"
import request from '@/utils/request'

export default {
  name: "Sku",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      skuList: [],
      spuOptions: [],
      searchLoading: false,
      title: "",
      open: false,
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
      form: {},
      rules: {
        spuId: [
          { required: true, message: "所属SPU不能为空", trigger: "change" }
        ],
        skuCode: [
          { required: true, message: "SKU编码不能为空", trigger: "blur" }
        ],
      },
      // 汇率缓存
      rates: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSku(this.queryParams).then(response => {
        this.skuList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    remoteMethod(query) {
      if (query !== '') {
        this.searchLoading = true;
        listSpu({ productName: query, pageNum: 1, pageSize: 20 }).then(response => {
          this.searchLoading = false;
          this.spuOptions = response.rows;
        });
      } else {
        this.spuOptions = [];
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
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
        createTime: null,
        targetMarkets: []
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
      this.title = "添加商品SKU"
      this.spuOptions = [];
      // 预加载汇率
      this.getRate('USD');
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getSku(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改商品SKU"
      })
    },
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
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除SKU编号为"' + ids + '"的数据项？').then(function() {
        return delSku(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('system/sku/export', {
        ...this.queryParams
      }, `sku_${new Date().getTime()}.xlsx`)
    },
    // 获取汇率
    getRate(currency) {
      if (this.rates[currency]) return; // 已有缓存
      request({
        url: '/system/spu/rate/' + currency,
        method: 'get'
      }).then(response => {
        this.$set(this.rates, currency, response.data);
      });
    },
    addTargetMarket() {
      this.form.targetMarkets.push({
        platform: 'AMAZON',
        country: 'US',
        priceMode: 'MULTIPLIER',
        inputValue: 1.5,
        price: 0
      });
      // 确保汇率已加载
      this.getRate('USD');
    },
    removeTargetMarket(index) {
      this.form.targetMarkets.splice(index, 1);
    },
    handleCountryChange(row) {
      // 切换国家时，重新获取汇率 (这里简化为只支持 USD，实际应根据国家映射货币)
      this.getRate('USD');
      this.calculateRowPrice(row);
    },
    calculateRowPrice(row) {
      if (row.priceMode === 'FIXED') {
        row.price = row.inputValue;
      } else {
        if (!this.form.purchasePrice) return;
        const rate = this.rates['USD'] || 1; // 默认 1
        const multiplier = row.inputValue || 1;
        row.price = (this.form.purchasePrice * rate * multiplier).toFixed(2);
      }
    },
    calculatePrices() {
      if (this.form.targetMarkets) {
        this.form.targetMarkets.forEach(item => {
          this.calculateRowPrice(item);
        });
      }
    }
  }
}
</script>
