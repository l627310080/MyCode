package com.john.cils.controller;

import com.john.cils.domain.CilsRuleConfig;
import com.john.cils.service.ICilsRuleConfigService;
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
 * 动态合规校验规则配置Controller
 *
 * @author john
 * @date 2026-01-09
 */
@RestController
@RequestMapping("/system/rule-config")
public class CilsRuleConfigController extends BaseController {
    @Autowired
    private ICilsRuleConfigService cilsRuleConfigService;

    /**
     * 查询动态合规校验规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:rule-config:list')")
    @GetMapping("/list")
    public TableDataInfo list(CilsRuleConfig cilsRuleConfig) {
        startPage();
        List<CilsRuleConfig> list = cilsRuleConfigService.selectCilsRuleConfigList(cilsRuleConfig);
        return getDataTable(list);
    }

    /**
     * 导出动态合规校验规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:rule-config:export')")
    @Log(title = "动态合规校验规则配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CilsRuleConfig cilsRuleConfig) {
        List<CilsRuleConfig> list = cilsRuleConfigService.selectCilsRuleConfigList(cilsRuleConfig);
        ExcelUtil<CilsRuleConfig> util = new ExcelUtil<CilsRuleConfig>(CilsRuleConfig.class);
        util.exportExcel(response, list, "动态合规校验规则配置数据");
    }

    /**
     * 获取动态合规校验规则配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:rule-config:query')")
    @GetMapping(value = "/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId) {
        return success(cilsRuleConfigService.selectCilsRuleConfigById(ruleId));
    }

    /**
     * 新增动态合规校验规则配置
     */
    @PreAuthorize("@ss.hasPermi('system:rule-config:add')")
    @Log(title = "动态合规校验规则配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsRuleConfig cilsRuleConfig) {
        return toAjax(cilsRuleConfigService.insertCilsRuleConfig(cilsRuleConfig));
    }

    /**
     * 修改动态合规校验规则配置
     */
    @PreAuthorize("@ss.hasPermi('system:rule-config:edit')")
    @Log(title = "动态合规校验规则配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsRuleConfig cilsRuleConfig) {
        return toAjax(cilsRuleConfigService.updateCilsRuleConfig(cilsRuleConfig));
    }

    /**
     * 删除动态合规校验规则配置
     */
    @PreAuthorize("@ss.hasPermi('system:rule-config:remove')")
    @Log(title = "动态合规校验规则配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds) {
        return toAjax(cilsRuleConfigService.deleteCilsRuleConfigByIds(ruleIds));
    }
}
