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
        >新增
        </el-button>
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
        >修改
        </el-button>
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
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:sku:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="skuList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="主键ID" align="center" prop="id" width="80"/>
      <el-table-column label="SPU ID" align="center" prop="spuId" width="100"/>
      <el-table-column label="SKU编码" align="center" prop="skuCode" width="150"/>
      <el-table-column label="规格描述" align="center" prop="specInfo" min-width="180" :show-overflow-tooltip="true"/>
      <el-table-column label="规格图" align="center" prop="skuImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.skuImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="采购价" align="center" prop="purchasePrice" width="100"/>
      <el-table-column label="库存" align="center" prop="stockQty" width="100"/>
      <el-table-column label="重量(kg)" align="center" prop="weightKg" width="100"/>
      <el-table-column label="审核状态" align="center" prop="isAudit" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isAudit === 0" type="info">待审核</el-tag>
          <el-tag v-else-if="scope.row.isAudit === 1" type="success">审核通过</el-tag>
          <el-tag v-else-if="scope.row.isAudit === 2" type="danger">合规拦截</el-tag>
          <span v-else>{{ scope.row.isAudit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="150" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:sku:edit']"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:sku:remove']"
          >删除
          </el-button>
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
                :loading="searchLoading"
                @change="handleSpuChange">
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
              <el-input v-model="form.skuCode" placeholder="填写规格描述后自动生成" readonly/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格描述" prop="specInfo">
              <el-input v-model="form.specInfo" type="textarea" placeholder="例如: 红色, XL" @input="generateSkuCode"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格图" prop="skuImage">
              <image-upload v-model="form.skuImage"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购价(RMB)" prop="purchasePrice" label-width="110px">
              <el-input
                v-model="form.purchasePrice"
                placeholder="请输入采购价"
                @input="handlePurchasePriceInput"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stockQty">
              <el-input v-model.number="form.stockQty" placeholder="请输入库存"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重量(kg)" prop="weightKg">
              <el-input v-model.number="form.weightKg" placeholder="请输入重量"/>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 一键铺货区域 -->
        <div>
          <el-divider content-position="left">一键铺货 (自动创建映射)</el-divider>
          <el-button type="primary" icon="el-icon-plus" size="mini" @click="addTargetMarket"
                     style="margin-bottom: 10px;">添加发布目标
          </el-button>
          <el-table :data="form.targetMarkets" border style="width: 100%" :key="tableKey">
            <el-table-column label="平台" width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.platform" placeholder="选择平台">
                  <el-option label="Amazon" value="AMAZON"/>
                  <el-option label="Shopee" value="SHOPEE"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="国家" width="150">
              <template slot-scope="scope">
                <el-select
                  v-model="scope.row.country"
                  placeholder="选择国家"
                  @change="handleCountryChange(scope.$index, scope.row.country)"
                >
                  <el-option label="美国 (US)" value="US"/>
                  <el-option label="英国 (UK)" value="UK"/>
                  <el-option label="泰国 (TH)" value="TH"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="定价模式" width="150">
              <template slot-scope="scope">
                <el-select
                  v-model="scope.row.priceMode"
                  placeholder="模式"
                  @change="handlePriceModeChange(scope.$index, scope.row.priceMode)"
                >
                  <el-option label="固定价格" value="FIXED"/>
                  <el-option label="倍数定价" value="MULTIPLIER"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数值 (价/倍)" width="150">
              <template slot-scope="scope">
                <el-input
                  v-model="scope.row.inputValue"
                  placeholder="价格或倍数"
                  @change="handleInputValueChange(scope.$index, scope.row.inputValue)"
                />
              </template>
            </el-table-column>
            <el-table-column label="最终售价" width="200">
              <template slot-scope="scope">
                <span style="color: #E6A23C; font-weight: bold;">
                  {{ scope.row.currency }} {{ formatPrice(scope.row.price) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="100">
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
import {addSku, delSku, getSku, listSku, updateSku} from "@/api/system/sku"
import {listSpu} from "@/api/system/spu"
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
      tableKey: 0, // 用于强制刷新表格
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
          {required: true, message: "所属SPU不能为空", trigger: "change"}
        ],
      },
      rates: {},
      countryCurrency: {
        'US': 'USD',
        'UK': 'GBP',
        'TH': 'THB'
      },
      selectedSpu: null
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
      this.searchLoading = true;
      listSpu({productName: query, isAudit: 1, pageNum: 1, pageSize: 20}).then(response => {
        this.searchLoading = false;
        this.spuOptions = response.rows;
      });
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
      this.selectedSpu = null;
      this.tableKey = 0;
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加商品SKU"
      this.remoteMethod('');
      // 预加载常用货币的汇率
      this.getRate('USD');
      this.getRate('GBP');
      this.getRate('THB');
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
      this.$modal.confirm('是否确认删除SKU编号为"' + ids + '"的数据项？').then(function () {
        return delSku(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {
      })
    },
    handleExport() {
      this.download('system/sku/export', {
        ...this.queryParams
      }, `sku_${new Date().getTime()}.xlsx`)
    },

    /**
     * 获取指定币种的汇率
     */
    getRate(currency) {
      if (this.rates[currency]) return Promise.resolve(this.rates[currency]);
      return request({
        url: '/system/spu/rate/' + currency,
        method: 'get'
      }).then(response => {
        this.$set(this.rates, currency, response.data);
        console.log(`✅ 成功获取${currency}汇率:`, response.data);
        return response.data;
      }).catch(error => {
        console.error(`❌ 获取${currency}汇率失败:`, error);
        // 设置默认汇率为1，避免计算出错
        this.$set(this.rates, currency, 1);
        return 1;
      });
    },

    /**
     * 添加目标市场
     */
    async addTargetMarket() {
      const newMarket = {
        platform: 'AMAZON',
        country: 'US',
        priceMode: 'MULTIPLIER',
        inputValue: 1.5,
        price: 0,
        currency: 'USD'
      };

      if (!this.form.targetMarkets) {
        this.$set(this.form, 'targetMarkets', []);
      }

      // 先获取汇率
      await this.getRate(newMarket.currency);

      // 添加新市场
      this.form.targetMarkets.push(newMarket);

      // 计算价格
      this.$nextTick(() => {
        this.calculateMarketPrice(this.form.targetMarkets.length - 1);
      });
    },

    /**
     * 删除目标市场
     */
    removeTargetMarket(index) {
      this.form.targetMarkets.splice(index, 1);
      this.forceTableUpdate();
    },

    /**
     * 处理采购价输入
     */
    handlePurchasePriceInput(val) {
      const price = val === '' ? null : parseFloat(val);
      this.$set(this.form, 'purchasePrice', price);
      console.log('📝 采购价输入:', price);

      // 延迟一下再计算，确保数据已更新
      this.$nextTick(() => {
        this.calculateAllMarketPrices();
      });
    },

    /**
     * 处理数值（价/倍）输入
     */
    handleInputValueChange(index, val) {
      if (!this.form.targetMarkets[index]) return;

      const inputValue = val === '' ? 0 : parseFloat(val);

      // 创建新对象，更新 inputValue
      const updatedMarket = {
        ...this.form.targetMarkets[index],
        inputValue: inputValue
      };

      // 替换整个对象
      this.$set(this.form.targetMarkets, index, updatedMarket);

      console.log(`📝 市场${index}数值输入:`, inputValue);

      // 立即重新计算该市场价格
      this.$nextTick(() => {
        this.calculateMarketPrice(index);
      });
    },

    /**
     * 国家变更：更新币种并重新计算价格
     */
    async handleCountryChange(index, country) {
      if (!this.form.targetMarkets[index]) return;

      console.log(`🌍 市场${index}国家变更为:`, country);

      const market = this.form.targetMarkets[index];
      const newCurrency = this.countryCurrency[country] || 'USD';

      // 创建新对象，更新国家和币种
      const updatedMarket = {
        ...market,
        country: country,
        currency: newCurrency
      };

      // 替换整个对象
      this.$set(this.form.targetMarkets, index, updatedMarket);

      // 获取新币种的汇率
      await this.getRate(newCurrency);

      // 重新计算价格
      this.$nextTick(() => {
        this.calculateMarketPrice(index);
      });
    },

    /**
     * 定价模式变更：重新计算价格
     */
    handlePriceModeChange(index, mode) {
      if (!this.form.targetMarkets[index]) return;

      console.log(`💰 市场${index}定价模式变更为:`, mode);

      // 创建新对象，更新定价模式
      const updatedMarket = {
        ...this.form.targetMarkets[index],
        priceMode: mode
      };

      // 替换整个对象
      this.$set(this.form.targetMarkets, index, updatedMarket);

      // 立即重新计算价格
      this.$nextTick(() => {
        this.calculateMarketPrice(index);
      });
    },

    /**
     * 计算单个市场的最终售价
     * @param {number} index - targetMarkets 的索引
     */
    calculateMarketPrice(index) {
      if (!this.form.targetMarkets || index < 0 || index >= this.form.targetMarkets.length) {
        console.warn('⚠️ 无效的市场索引:', index);
        return;
      }

      const market = this.form.targetMarkets[index];
      const purchasePrice = this.form.purchasePrice;
      const rate = this.rates[market.currency] || 1;
      const inputValue = market.inputValue || 0;

      console.log(`🧮 计算市场${index}价格:`, {
        country: market.country,
        currency: market.currency,
        priceMode: market.priceMode,
        inputValue: inputValue,
        purchasePrice: purchasePrice,
        rate: rate
      });

      let newPrice = 0;

      if (market.priceMode === 'FIXED') {
        // 固定价格模式：直接使用输入值
        newPrice = inputValue;
        console.log(`  ➜ 固定价格模式: ${inputValue}`);
      } else {
        // 倍数定价模式：采购价 × 汇率 × 倍数
        if (!purchasePrice || purchasePrice <= 0) {
          newPrice = 0;
          console.log(`  ➜ 采购价无效，价格为0`);
        } else {
          newPrice = parseFloat((purchasePrice * rate * inputValue).toFixed(2));
          console.log(`  ➜ 倍数定价: ${purchasePrice} × ${rate} × ${inputValue} = ${newPrice}`);
        }
      }

      // 创建新对象替换整个市场对象，确保响应式更新
      const updatedMarket = {
        ...market,
        price: newPrice
      };

      // 使用 $set 替换整个对象
      this.$set(this.form.targetMarkets, index, updatedMarket);

      console.log(`✅ 市场${index}最终价格: ${updatedMarket.currency} ${newPrice}`);

      // 强制刷新表格
      this.forceTableUpdate();
    },

    /**
     * 采购价改变时，计算所有市场的价格
     */
    calculateAllMarketPrices() {
      if (!this.form.targetMarkets || !this.form.targetMarkets.length) {
        console.log('⚠️ 没有目标市场需要计算');
        return;
      }

      console.log('🔄 重新计算所有市场价格，采购价:', this.form.purchasePrice);

      for (let i = 0; i < this.form.targetMarkets.length; i++) {
        this.calculateMarketPrice(i);
      }
    },

    /**
     * 强制更新表格
     */
    forceTableUpdate() {
      this.tableKey++;
    },

    /**
     * 格式化价格显示
     */
    formatPrice(price) {
      if (!price && price !== 0) return '0.00';
      return parseFloat(price).toFixed(2);
    },

    /**
     * SPU变更：更新选中的SPU对象，用于生成SKU编码
     */
    handleSpuChange(spuId) {
      this.selectedSpu = this.spuOptions.find(item => item.id === spuId);
      this.generateSkuCode();
    },

    /**
     * 生成SKU编码
     */
    generateSkuCode() {
      if (this.selectedSpu && this.form.specInfo) {
        const spec = this.form.specInfo.replace(/[,，\s]/g, '-');
        this.form.skuCode = `${this.selectedSpu.spuCode}-${spec}`;
      }
    }
  }
}
</script>
