package com.john.cils.controller;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.service.ExchangeRateService;
import com.john.cils.service.ICilsProductSpuService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 跨境商品标准信息表(SPU)Controller
 * 
 * @author john
 * @date 2024-05-20
 */
@RestController 
@RequestMapping("/system/spu") 
public class CilsProductSpuController extends BaseController {
    @Autowired
    private ICilsProductSpuService cilsProductSpuService;
    
    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * 查询跨境商品标准信息表(SPU)列表
     */
    @PreAuthorize("@ss.hasPermi('system:spu:list')") 
    @GetMapping("/list")
    public TableDataInfo list(CilsProductSpu cilsProductSpu) {
        startPage(); 
        List<CilsProductSpu> list = cilsProductSpuService.selectCilsProductSpuList(cilsProductSpu);
        return getDataTable(list); 
    }

    /**
     * 获取SPU选项列表 (用于下拉搜索)
     */
    @GetMapping("/optionList")
    public AjaxResult optionList(CilsProductSpu cilsProductSpu) {
        List<CilsProductSpu> list = cilsProductSpuService.selectCilsProductSpuList(cilsProductSpu);
        return success(list);
    }
    
    /**
     * 获取指定货币的汇率
     */
    @GetMapping("/rate/{currency}")
    public AjaxResult getRate(@PathVariable("currency") String currency) {
        BigDecimal rate = exchangeRateService.getRate(currency);
        return success(rate);
    }

    /**
     * 导出跨境商品标准信息表(SPU)列表
     */
    @PreAuthorize("@ss.hasPermi('system:spu:export')")
    @Log(title = "跨境商品标准信息表(SPU)", businessType = BusinessType.EXPORT) 
    @PostMapping("/export")
    public void export(HttpServletResponse response, CilsProductSpu cilsProductSpu) {
        List<CilsProductSpu> list = cilsProductSpuService.selectCilsProductSpuList(cilsProductSpu);
        ExcelUtil<CilsProductSpu> util = new ExcelUtil<CilsProductSpu>(CilsProductSpu.class);
        util.exportExcel(response, list, "跨境商品标准信息表(SPU)数据");
    }

    /**
     * 获取跨境商品标准信息表(SPU)详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:spu:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cilsProductSpuService.selectCilsProductSpuById(id));
    }

    /**
     * 新增跨境商品标准信息表(SPU)
     */
    @PreAuthorize("@ss.hasPermi('system:spu:add')")
    @Log(title = "跨境商品标准信息表(SPU)", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsProductSpu cilsProductSpu) {
        // 强制校验：必须包含标题和主图
        if (StringUtils.isEmpty(cilsProductSpu.getProductName())) {
            return error("商品标题不能为空");
        }
        if (StringUtils.isEmpty(cilsProductSpu.getMainImage())) {
            return error("商品主图不能为空");
        }
        
        return toAjax(cilsProductSpuService.insertCilsProductSpu(cilsProductSpu));
    }

    /**
     * 修改跨境商品标准信息表(SPU)
     */
    @PreAuthorize("@ss.hasPermi('system:spu:edit')")
    @Log(title = "跨境商品标准信息表(SPU)", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsProductSpu cilsProductSpu) {
        // 修改时也要校验
        if (StringUtils.isEmpty(cilsProductSpu.getProductName())) {
            return error("商品标题不能为空");
        }
        if (StringUtils.isEmpty(cilsProductSpu.getMainImage())) {
            return error("商品主图不能为空");
        }
        return toAjax(cilsProductSpuService.updateCilsProductSpu(cilsProductSpu));
    }

    /**
     * 删除跨境商品标准信息表(SPU)
     */
    @PreAuthorize("@ss.hasPermi('system:spu:remove')")
    @Log(title = "跨境商品标准信息表(SPU)", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cilsProductSpuService.deleteCilsProductSpuByIds(ids));
    }
}
