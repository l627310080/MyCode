package com.john.cils.controller;

import com.john.cils.domain.CilsCategory;
import com.john.cils.service.ICilsCategoryService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 商品类目Controller
 *
 * @author john
 * @date 2026-01-09
 */
@RestController
@RequestMapping("/system/category")
public class CilsCategoryController extends BaseController {
    @Autowired
    private ICilsCategoryService cilsCategoryService;

    /**
     * 查询商品类目列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:list')")
    @GetMapping("/list")
    public AjaxResult list(CilsCategory cilsCategory) {
        List<CilsCategory> list = cilsCategoryService.selectCilsCategoryList(cilsCategory);
        return success(list);
    }

    /**
     * 获取类目下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(CilsCategory cilsCategory) {
        List<CilsCategory> categorys = cilsCategoryService.selectCilsCategoryList(cilsCategory);
        return success(cilsCategoryService.buildCategoryTreeSelect(categorys));
    }

    /**
     * 导出商品类目列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:export')")
    @Log(title = "商品类目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CilsCategory cilsCategory) {
        List<CilsCategory> list = cilsCategoryService.selectCilsCategoryList(cilsCategory);
        ExcelUtil<CilsCategory> util = new ExcelUtil<CilsCategory>(CilsCategory.class);
        util.exportExcel(response, list, "商品类目数据");
    }

    /**
     * 获取商品类目详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return success(cilsCategoryService.selectCilsCategoryByCategoryId(categoryId));
    }

    /**
     * 新增商品类目
     */
    @PreAuthorize("@ss.hasPermi('system:category:add')")
    @Log(title = "商品类目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsCategory cilsCategory) {
        return toAjax(cilsCategoryService.insertCilsCategory(cilsCategory));
    }

    /**
     * 修改商品类目
     */
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "商品类目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsCategory cilsCategory) {
        return toAjax(cilsCategoryService.updateCilsCategory(cilsCategory));
    }

    /**
     * 删除商品类目
     */
    @PreAuthorize("@ss.hasPermi('system:category:remove')")
    @Log(title = "商品类目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds) {
        return toAjax(cilsCategoryService.deleteCilsCategoryByCategoryIds(categoryIds));
    }
}
