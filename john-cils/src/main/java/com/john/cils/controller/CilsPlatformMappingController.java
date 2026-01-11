package com.john.cils.controller;

import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.service.ICilsPlatformMappingService;
import com.john.cils.service.PlatformPushService;
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
 * 跨平台商品映射Controller
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@RestController
@RequestMapping("/system/mapping")
public class CilsPlatformMappingController extends BaseController {
    @Autowired
    private ICilsPlatformMappingService cilsPlatformMappingService;

    @Autowired
    private PlatformPushService platformPushService;

    /**
     * 查询跨平台商品映射列表
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:list')")
    @GetMapping("/list")
    public TableDataInfo list(CilsPlatformMapping cilsPlatformMapping) {
        startPage();
        List<CilsPlatformMapping> list = cilsPlatformMappingService.selectCilsPlatformMappingList(cilsPlatformMapping);
        return getDataTable(list);
    }

    /**
     * 导出跨平台商品映射列表
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:export')")
    @Log(title = "跨平台商品映射", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CilsPlatformMapping cilsPlatformMapping) {
        List<CilsPlatformMapping> list = cilsPlatformMappingService.selectCilsPlatformMappingList(cilsPlatformMapping);
        ExcelUtil<CilsPlatformMapping> util = new ExcelUtil<CilsPlatformMapping>(CilsPlatformMapping.class);
        util.exportExcel(response, list, "跨平台商品映射数据");
    }

    /**
     * 获取跨平台商品映射详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cilsPlatformMappingService.selectCilsPlatformMappingById(id));
    }

    /**
     * 新增跨平台商品映射
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:add')")
    @Log(title = "跨平台商品映射", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsPlatformMapping cilsPlatformMapping) {
        return toAjax(cilsPlatformMappingService.insertCilsPlatformMapping(cilsPlatformMapping));
    }

    /**
     * 修改跨平台商品映射
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:edit')")
    @Log(title = "跨平台商品映射", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsPlatformMapping cilsPlatformMapping) {
        return toAjax(cilsPlatformMappingService.updateCilsPlatformMapping(cilsPlatformMapping));
    }

    /**
     * 删除跨平台商品映射
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:remove')")
    @Log(title = "跨平台商品映射", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cilsPlatformMappingService.deleteCilsPlatformMappingByIds(ids));
    }

    /**
     * 手动触发推送
     */
    @PreAuthorize("@ss.hasPermi('system:mapping:edit')")
    @Log(title = "跨平台商品映射", businessType = BusinessType.OTHER)
    @PostMapping("/push/{id}")
    public AjaxResult push(@PathVariable("id") Long id) {
        platformPushService.pushProduct(id);
        return success("推送任务已提交");
    }
}
