package com.john.cils.controller;

import com.john.cils.domain.CilsProductSku;
import com.john.cils.service.ICilsProductSkuService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
        // 调用 service 方法，该方法会通过 mybatis 的 useGeneratedKeys 将 id 回填到 cilsProductSku 对象中
        cilsProductSkuService.insertCilsProductSku(cilsProductSku);
        // 将包含新ID的完整对象返回给前端，用于轮询
        return AjaxResult.success(cilsProductSku);
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
     * 模拟黑五等大促的接口
     * ————这个接口其实是在模拟黑五等大促活动时，同步亚马逊等平台告诉它们我们还有多少库存
     *      形成的就是redis接住来自平台的库存-1的消息，如果卖完了，就告诉平台；接下来由kafka接管，保证数据正确
     *      也配合当前代码实际业务情况，设计了一个自定义线程池模拟同时扣减多个平台的库存的情况
     */
    @PostMapping("/deduct")
    public AjaxResult deductStock(@RequestParam Long skuId, @RequestParam Integer quantity) {
//        System.out.println(">>>> [CONTROLLER] 收到扣减请求: skuId=" + skuId + ", qty=" + quantity);
        boolean success = cilsProductSkuService.deductStock(skuId, quantity);
        if (success) {
            return success("库存扣减成功");
        } else {
            return error("库存不足或扣减失败");
        }
    }
}
