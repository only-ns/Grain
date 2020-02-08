package com.only.grain.api.service;

import com.only.grain.api.bean.PmsProductImage;
import com.only.grain.api.bean.PmsProductInfo;
import com.only.grain.api.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    public List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);
}
