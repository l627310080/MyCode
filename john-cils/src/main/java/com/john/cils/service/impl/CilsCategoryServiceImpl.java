package com.john.cils.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.john.cils.mapper.CilsCategoryMapper;
import com.john.cils.domain.CilsCategory;
import com.john.cils.service.ICilsCategoryService;

/**
 * 商品类目Service业务层处理
 * 
 * @author john
 * @date 2026-01-09
 */
@Service
public class CilsCategoryServiceImpl implements ICilsCategoryService 
{
    @Autowired
    private CilsCategoryMapper cilsCategoryMapper;

    /**
     * 查询商品类目
     * 
     * @param categoryId 商品类目主键
     * @return 商品类目
     */
    @Override
    public CilsCategory selectCilsCategoryByCategoryId(Long categoryId)
    {
        return cilsCategoryMapper.selectCilsCategoryByCategoryId(categoryId);
    }

    /**
     * 查询商品类目列表
     * 
     * @param cilsCategory 商品类目
     * @return 商品类目
     */
    @Override
    public List<CilsCategory> selectCilsCategoryList(CilsCategory cilsCategory)
    {
        return cilsCategoryMapper.selectCilsCategoryList(cilsCategory);
    }

    /**
     * 构建前端所需要树结构
     */
    @Override
    public List<CilsCategory> buildCategoryTree(List<CilsCategory> categorys)
    {
        List<CilsCategory> returnList = new ArrayList<CilsCategory>();
        List<Long> tempList = new ArrayList<Long>();
        for (CilsCategory category : categorys)
        {
            tempList.add(category.getCategoryId());
        }
        for (Iterator<CilsCategory> iterator = categorys.iterator(); iterator.hasNext();)
        {
            CilsCategory category = (CilsCategory) iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(category.getParentId()))
            {
                recursionFn(categorys, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = categorys;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     */
    @Override
    public List<TreeSelect> buildCategoryTreeSelect(List<CilsCategory> categorys)
    {
        List<CilsCategory> categoryTrees = buildCategoryTree(categorys);
        // 手动转换，不再使用构造函数引用
        return categoryTrees.stream().map(category -> {
            TreeSelect ts = new TreeSelect();
            ts.setId(category.getCategoryId());
            ts.setLabel(category.getCategoryName());
            ts.setChildren(category.getChildren().stream().map(child -> {
                TreeSelect childTs = new TreeSelect();
                childTs.setId(child.getCategoryId());
                childTs.setLabel(child.getCategoryName());
                // 如果还有子节点，可以继续递归
                return childTs;
            }).collect(Collectors.toList()));
            return ts;
        }).collect(Collectors.toList());
    }

    /**
     * 新增商品类目
     * 
     * @param cilsCategory 商品类目
     * @return 结果
     */
    @Override
    public int insertCilsCategory(CilsCategory cilsCategory)
    {
        cilsCategory.setCreateTime(DateUtils.getNowDate());
        return cilsCategoryMapper.insertCilsCategory(cilsCategory);
    }

    /**
     * 修改商品类目
     * 
     * @param cilsCategory 商品类目
     * @return 结果
     */
    @Override
    public int updateCilsCategory(CilsCategory cilsCategory)
    {
        cilsCategory.setUpdateTime(DateUtils.getNowDate());
        return cilsCategoryMapper.updateCilsCategory(cilsCategory);
    }

    /**
     * 批量删除商品类目
     * 
     * @param categoryIds 需要删除的商品类目主键
     * @return 结果
     */
    @Override
    public int deleteCilsCategoryByCategoryIds(Long[] categoryIds)
    {
        return cilsCategoryMapper.deleteCilsCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除商品类目信息
     * 
     * @param categoryId 商品类目主键
     * @return 结果
     */
    @Override
    public int deleteCilsCategoryByCategoryId(Long categoryId)
    {
        return cilsCategoryMapper.deleteCilsCategoryByCategoryId(categoryId);
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<CilsCategory> list, CilsCategory t)
    {
        // 得到子节点列表
        List<CilsCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (CilsCategory tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<CilsCategory> getChildList(List<CilsCategory> list, CilsCategory t)
    {
        List<CilsCategory> tlist = new ArrayList<CilsCategory>();
        Iterator<CilsCategory> it = list.iterator();
        while (it.hasNext())
        {
            CilsCategory n = (CilsCategory) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getCategoryId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<CilsCategory> list, CilsCategory t)
    {
        return getChildList(list, t).size() > 0;
    }
}
