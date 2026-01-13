package com.john.cils.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils; // 引入 SecurityUtils
import com.john.cils.domain.CilsProductSku;
import com.john.cils.service.ICilsProductSkuService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 商品单品规格表(SKU)Controller
 * 
 * @author john
 * @date 2024-05-20
 */
@RestController
@RequestMapping("/system/sku")
public class CilsProductSkuController extends BaseController {
    @Autowired
    private ICilsProductSkuService cilsProductSkuService;

    /**
     * 查询商品单品规格(SKU)列表
     */
    @PreAuthorize("@ss.hasPermi('system:sku:list')")
    @GetMapping("/list")
    public TableDataInfo list(CilsProductSku cilsProductSku) {
        startPage();
        List<CilsProductSku> list = cilsProductSkuService.selectCilsProductSkuList(cilsProductSku);
        return getDataTable(list);
    }

    /**
     * 导出商品单品规格(SKU)列表
     */
    @PreAuthorize("@ss.hasPermi('system:sku:export')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CilsProductSku cilsProductSku) {
        List<CilsProductSku> list = cilsProductSkuService.selectCilsProductSkuList(cilsProductSku);
        ExcelUtil<CilsProductSku> util = new ExcelUtil<CilsProductSku>(CilsProductSku.class);
        util.exportExcel(response, list, "商品单品规格(SKU)数据");
    }

    /**
     * 获取商品单品规格(SKU)详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:sku:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cilsProductSkuService.selectCilsProductSkuById(id));
    }

    /**
     * 新增商品单品规格(SKU)
     */
    @PreAuthorize("@ss.hasPermi('system:sku:add')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsProductSku cilsProductSku) {
        // 强制设置创建者
        cilsProductSku.setCreateBy(SecurityUtils.getUsername());
        return toAjax(cilsProductSkuService.insertCilsProductSku(cilsProductSku));
    }

    /**
     * 修改商品单品规格(SKU)
     */
    @PreAuthorize("@ss.hasPermi('system:sku:edit')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsProductSku cilsProductSku) {
        // 强制设置更新者
        cilsProductSku.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(cilsProductSkuService.updateCilsProductSku(cilsProductSku));
    }

    /**
     * 删除商品单品规格(SKU)
     */
    @PreAuthorize("@ss.hasPermi('system:sku:remove')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cilsProductSkuService.deleteCilsProductSkuByIds(ids));
    }
    
    /**
     * 扣减库存 (高并发接口)
     */
    @PostMapping("/deduct")
    public AjaxResult deductStock(@RequestParam Long skuId, @RequestParam Integer quantity) {
        boolean success = cilsProductSkuService.deductStock(skuId, quantity);
        if (success) {
            return success("库存扣减成功");
        } else {
            return error("库存不足或扣减失败");
        }
    }
}
