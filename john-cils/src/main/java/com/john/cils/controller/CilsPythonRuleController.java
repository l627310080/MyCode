package com.john.cils.controller;

import com.john.cils.domain.CilsPythonRule;
import com.john.cils.service.ICilsPythonRuleService;
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
 * Python合规校验规则配置Controller
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@RestController
@RequestMapping("/system/rule")
public class CilsPythonRuleController extends BaseController {
    @Autowired
    private ICilsPythonRuleService cilsPythonRuleService;

    /**
     * 查询Python合规校验规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:rule:list')")
    @GetMapping("/list")
    public TableDataInfo list(CilsPythonRule cilsPythonRule) {
        startPage();
        List<CilsPythonRule> list = cilsPythonRuleService.selectCilsPythonRuleList(cilsPythonRule);
        return getDataTable(list);
    }

    /**
     * 导出Python合规校验规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:rule:export')")
    @Log(title = "Python合规校验规则配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CilsPythonRule cilsPythonRule) {
        List<CilsPythonRule> list = cilsPythonRuleService.selectCilsPythonRuleList(cilsPythonRule);
        ExcelUtil<CilsPythonRule> util = new ExcelUtil<CilsPythonRule>(CilsPythonRule.class);
        util.exportExcel(response, list, "Python合规校验规则配置数据");
    }

    /**
     * 获取Python合规校验规则配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:rule:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cilsPythonRuleService.selectCilsPythonRuleById(id));
    }

    /**
     * 新增Python合规校验规则配置
     */
    @PreAuthorize("@ss.hasPermi('system:rule:add')")
    @Log(title = "Python合规校验规则配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsPythonRule cilsPythonRule) {
        return toAjax(cilsPythonRuleService.insertCilsPythonRule(cilsPythonRule));
    }

    /**
     * 修改Python合规校验规则配置
     */
    @PreAuthorize("@ss.hasPermi('system:rule:edit')")
    @Log(title = "Python合规校验规则配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsPythonRule cilsPythonRule) {
        return toAjax(cilsPythonRuleService.updateCilsPythonRule(cilsPythonRule));
    }

    /**
     * 删除Python合规校验规则配置
     */
    @PreAuthorize("@ss.hasPermi('system:rule:remove')")
    @Log(title = "Python合规校验规则配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cilsPythonRuleService.deleteCilsPythonRuleByIds(ids));
    }
}
