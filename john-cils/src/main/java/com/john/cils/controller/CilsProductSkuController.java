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
 * <p>
 * 作用：
 * 接收前端关于 SKU 的 HTTP 请求，调用 Service 层处理业务，并返回 JSON 结果。
 * <p>
 * 注解说明:
 * - @RestController: 标识这是一个 RESTful 风格的控制器，所有方法返回 JSON 数据。
 * - @RequestMapping("/system/sku"): 定义该控制器下所有请求的基础 URL 路径。
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
     * <p>
     * 注解说明:
     * - @PreAuthorize: 在方法执行前检查用户是否拥有 'system:sku:list' 权限。
     * - @GetMapping: 将 HTTP GET 请求映射到此方法。
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
     * <p>
     * 注解说明:
     * - @Log: 记录操作日志。
     * - @PostMapping: 将 HTTP POST 请求映射到此方法。
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
     * <p>
     * 注解说明:
     * - @GetMapping(value = "/{id}"): 将 HTTP GET 请求映射到此方法，并从 URL 路径中提取 id。
     * - @PathVariable: 将 URL 中的 {id} 绑定到方法参数 id。
     */
    @PreAuthorize("@ss.hasPermi('system:sku:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cilsProductSkuService.selectCilsProductSkuById(id));
    }

    /**
     * 新增商品单品规格(SKU)
     * <p>
     * 注解说明:
     * - @PostMapping: 将 HTTP POST 请求映射到此方法。
     * - @RequestBody: 将请求体中的 JSON 数据转换为 CilsProductSku 对象。
     */
    @PreAuthorize("@ss.hasPermi('system:sku:add')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CilsProductSku cilsProductSku) {
        return toAjax(cilsProductSkuService.insertCilsProductSku(cilsProductSku));
    }

    /**
     * 修改商品单品规格(SKU)
     * <p>
     * 注解说明:
     * - @PutMapping: 将 HTTP PUT 请求映射到此方法，通常用于更新操作。
     */
    @PreAuthorize("@ss.hasPermi('system:sku:edit')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsProductSku cilsProductSku) {
        return toAjax(cilsProductSkuService.updateCilsProductSku(cilsProductSku));
    }

    /**
     * 删除商品单品规格(SKU)
     * <p>
     * 注解说明:
     * - @DeleteMapping: 将 HTTP DELETE 请求映射到此方法。
     */
    @PreAuthorize("@ss.hasPermi('system:sku:remove')")
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cilsProductSkuService.deleteCilsProductSkuByIds(ids));
    }

    /**
     * 扣减库存 (下单接口)
     * <p>
     * 作用：
     * 提供给前端或外部系统调用的库存扣减接口。
     * <p>
     * 注解说明:
     * - @PostMapping("/deduct"): 映射路径为 /system/sku/deduct
     * - @RequestParam: 从请求参数中获取 skuId 和 quantity
     */
    @PreAuthorize("@ss.hasPermi('system:sku:edit')") // 暂时复用编辑权限，实际应有专门的下单权限
    @Log(title = "商品单品规格(SKU)", businessType = BusinessType.UPDATE)
    @PostMapping("/deduct")
    public AjaxResult deduct(@RequestParam Long skuId, @RequestParam Integer quantity) {
        boolean success = cilsProductSkuService.deductStock(skuId, quantity);
        if (success) {
            return AjaxResult.success("库存扣减成功");
        } else {
            return AjaxResult.error("库存不足，扣减失败");
        }
    }
}
