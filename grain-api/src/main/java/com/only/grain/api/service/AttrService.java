package com.only.grain.api.service;

import com.only.grain.api.bean.*;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);
    List<PmsBaseAttrValue> AttrValueList(String attrInfoId);
    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);
    List<PmsBaseSaleAttr> baseSaleAttrList();
    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);
}
