package com.only.grain.api.service;

import com.only.grain.api.bean.PmsSkuInfo;
import com.only.grain.api.bean.PmsSkuSaleAttrValue;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfoById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku(String catalog3Id);

    List<PmsSkuSaleAttrValue> getSkuSaleAttrValueListBySku(String skuId);

    boolean checkPrice(String productSkuId, BigDecimal price);
}
