package com.john.cils.controller;

import com.john.cils.domain.CilsProductSpu;
import com.john.cils.service.ICilsProductSpuService;
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
 * 跨境商品标准信息表(SPU)Controller
 * <p>
 * 作用：
 * 接收前端的 HTTP 请求，调用 Service 层处理业务，并返回 JSON 结果。
 * 它是后端的“门户”。
 *
 * @author john
 * @date 2024-05-20
 */
@RestController // 标识这是一个 RESTful 风格的控制器，返回 JSON 数据
@RequestMapping("/system/spu") // 定义该控制器的基础 URL 路径
public class CilsProductSpuController extends BaseController {
    @Autowired
    private ICilsProductSpuService cilsProductSpuService;

    /**
     * 查询跨境商品标准信息表(SPU)列表
     */
    @PreAuthorize("@ss.hasPermi('system:spu:list')") // 权限控制：只有拥有 'system:spu:list' 权限的用户才能访问
    @GetMapping("/list")
    public TableDataInfo list(CilsProductSpu cilsProductSpu) {
        startPage(); // 开启分页
        List<CilsProductSpu> list = cilsProductSpuService.selectCilsProductSpuList(cilsProductSpu);
        return getDataTable(list); // 封装分页结果返回
    }

    /**
     * 导出跨境商品标准信息表(SPU)列表
     */
    @PreAuthorize("@ss.hasPermi('system:spu:export')")
    @Log(title = "跨境商品标准信息表(SPU)", businessType = BusinessType.EXPORT) // 记录操作日志
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
        // 调用 Service 的 insert 方法，这里会触发异步校验逻辑
        return toAjax(cilsProductSpuService.insertCilsProductSpu(cilsProductSpu));
    }

    /**
     * 修改跨境商品标准信息表(SPU)
     */
    @PreAuthorize("@ss.hasPermi('system:spu:edit')")
    @Log(title = "跨境商品标准信息表(SPU)", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CilsProductSpu cilsProductSpu) {
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
