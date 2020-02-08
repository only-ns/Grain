package com.only.grain.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.only.grain.api.bean.PmsProductImage;
import com.only.grain.api.bean.PmsProductInfo;
import com.only.grain.api.bean.PmsProductSaleAttr;
import com.only.grain.api.bean.PmsProductSaleAttrValue;
import com.only.grain.api.service.SpuService;
import com.only.grain.manage.mapper.PmsProductImageMapper;
import com.only.grain.manage.mapper.PmsProductInfoMapper;
import com.only.grain.manage.mapper.PmsProductSaleAttrMapper;
import com.only.grain.manage.mapper.PmsProductSaleAttrValueMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfoList = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfoList;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> imageList = pmsProductImageMapper.select(pmsProductImage);
        return imageList;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {
        List<PmsProductSaleAttr> saleAttrList = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId, skuId);

       /* PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(productId);
        List<PmsProductSaleAttr> saleAttrList = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr saleAttr : saleAttrList) {
            String id = saleAttr.getId();
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(saleAttr.getProductId());
            pmsProductSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());

            List<PmsProductSaleAttrValue> saleAttrValueList = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(saleAttrValueList);

        }*/
        return saleAttrList;
    }


}
