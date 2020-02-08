package com.only.grain.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.only.grain.api.bean.PmsProductSaleAttr;
import com.only.grain.api.bean.PmsSkuInfo;
import com.only.grain.api.bean.PmsSkuSaleAttrValue;
import com.only.grain.api.service.SkuService;
import com.only.grain.api.service.SpuService;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ItemController {
    @Reference
    private SkuService skuService;
    @Reference
    private SpuService spuService;

    @RequestMapping(value = "{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap){
        //sku
        PmsSkuInfo pmsSkuInfo= skuService.getSkuInfoById(skuId);
        modelMap.put("skuInfo",pmsSkuInfo);
        //销售属性列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);

        // 查询当前sku的spu的其他sku的集合的hash表
        Map<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());

        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";// "239|245"
            }
            skuSaleAttrHash.put(k,v);
        }

        // 将sku的销售属性hash表放到页面
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);

        return "item";
    }
    @Deprecated
    @RequestMapping(value = "index.html")
    public String index(ModelMap modelMap){
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            arrayList.add(i);
        }
        modelMap.put("arrayList",arrayList);
        modelMap.put("hello","hello thymeleaf");
        return "item";
    }
}
