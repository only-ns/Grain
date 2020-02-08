package com.only.grain.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.only.grain.api.bean.*;
import com.only.grain.api.service.AttrService;
import com.only.grain.manage.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class AttrServiceImpl implements AttrService {
    //平台属性Mapper
    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    //平台属性值Mapper
    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    //平台销售属性字典Mapper
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;
    //销售信息Mapper
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    //销售属性值Mapper
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    //销售属性Mapper
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    //商品图片Mapper
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> attrInfoList = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        for (PmsBaseAttrInfo attrInfo : attrInfoList) {
            PmsBaseAttrValue attrValue = new PmsBaseAttrValue();
            attrValue.setAttrId(attrInfo.getId());
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrValueMapper.select(attrValue);
            attrInfo.setAttrValueList(attrValueList);
        }
        return attrInfoList;
    }

    @Override
    public List<PmsBaseAttrValue> AttrValueList(String attrInfoId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrInfoId);
        List<PmsBaseAttrValue> attrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return attrValues;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id = pmsBaseAttrInfo.getId();
        if (StringUtils.isEmpty(id)) {
            //id为空 保存
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue attrValue : attrValueList) {
                attrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(attrValue);
            }
        }else{
            //id不为空 修改
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",id);
            pmsBaseAttrInfoMapper.updateByExample(pmsBaseAttrInfo,example);
            //删除属性值
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(id);
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
            //保存新的属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue attrValue : attrValueList) {
                attrValue.setAttrId(id);
                pmsBaseAttrValueMapper.insertSelective(attrValue);
            }

        }
        return "success";
    }

    /**
     *
     * @return 返回平台销售属性字典
     */
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> saleAttrList = pmsBaseSaleAttrMapper.selectAll();
        return saleAttrList;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        String id = pmsProductInfo.getId();
        if (StringUtils.isEmpty(id)){
            //保存商品信息
            pmsProductInfoMapper.insertSelective(pmsProductInfo);

        }else {
            //修改商品信息
            Example example = new Example(PmsProductInfo.class);
            example.createCriteria().andEqualTo("id",id);
            pmsProductInfoMapper.updateByExample(pmsProductInfo,example);
            //删除该商品属性
            PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
            pmsProductSaleAttr.setProductId(id);
            pmsProductSaleAttrMapper.delete(pmsProductSaleAttr);
            //删除该商品属性值
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(id);
            pmsProductSaleAttrValueMapper.delete(pmsProductSaleAttrValue);
            //删除该商品图片
            PmsProductImage pmsProductImage = new PmsProductImage();
            pmsProductImage.setProductId(id);
            pmsProductImageMapper.delete(pmsProductImage);

        }
        String saveId=pmsProductInfo.getId();
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        //保存商品属性
        if (spuSaleAttrList!=null) {
            for (PmsProductSaleAttr saleAttr : spuSaleAttrList) {
                saleAttr.setProductId(saveId);
                pmsProductSaleAttrMapper.insertSelective(saleAttr);
                List<PmsProductSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                //保存商品属性值
                if (spuSaleAttrList != null) {
                    for (PmsProductSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                        saleAttrValue.setProductId(saveId);
                        pmsProductSaleAttrValueMapper.insertSelective(saleAttrValue);
                    }
                }

            }
        }
        //保存图片属性
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        if (spuImageList!=null){
            for (PmsProductImage productImage : spuImageList) {
                productImage.setProductId(saveId);
                pmsProductImageMapper.insertSelective(productImage);
            }
        }
        return "success";
    }

    /**
     * 获取该商品销售属性列表
     * @param spuId
     * @return
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> saleAttrList = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr saleAttr : saleAttrList) {
            PmsProductSaleAttrValue attrValue = new PmsProductSaleAttrValue();
            attrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            attrValue.setProductId(spuId);
            List<PmsProductSaleAttrValue> attrValueList = pmsProductSaleAttrValueMapper.select(attrValue);
            saleAttr.setSpuSaleAttrValueList(attrValueList);
        }
        return saleAttrList;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {
        if (!valueIdSet.isEmpty()) {
            String valueIdStr = StringUtils.join(valueIdSet, ",");//41,45,46
            List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);
            return pmsBaseAttrInfos;
        }else{
            return new ArrayList<PmsBaseAttrInfo>();
        }

    }
}
