package com.john.cils.controller;

import com.john.cils.domain.CilsProductSku;
import com.john.cils.service.ICilsProductSkuService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 商品单品规格(SKU)Controller
 *
 * @author ruoyi
 * @date 2026-01-06
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
        return toAjax(cilsProductSkuService.insertCilsProductSku(cilsProductSku));
    }

    /**
     * 修改商品单品规格(SKU)
     */
    @PreAuthorize("@ss.hasPermi('system:sku:edit')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsProductSku cilsProductSku) {
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
}
