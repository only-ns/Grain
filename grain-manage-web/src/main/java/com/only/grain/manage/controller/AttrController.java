package com.only.grain.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.api.bean.*;
import com.only.grain.api.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {
    @Reference
    private AttrService attrService;

    /**
            * 获取平台属性信息
     * @param catalog3Id
     * @return
             */
    @RequestMapping(value = "attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.attrInfoList(catalog3Id);
        return pmsBaseAttrInfos;
    }

    /**
     * 获取销售属性信息
     * @param spuId
     * @return
     */
    @RequestMapping(value = "spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        List<PmsProductSaleAttr> saleAttrList = attrService.spuSaleAttrList(spuId);
        return saleAttrList;
    }
    @RequestMapping(value = "getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){
        List<PmsBaseAttrValue> pmsBaseAttrValues = attrService.AttrValueList(attrId);
        return pmsBaseAttrValues;
    }

    @RequestMapping(value = "saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        String status= attrService.saveAttrInfo(pmsBaseAttrInfo);
        return status;
    }
    @RequestMapping(value = "baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> saleAttrList= attrService.baseSaleAttrList();
        return saleAttrList;
    }

    /**
     * 保存销售商品Sku
     * @return
     */
    @RequestMapping(value = "saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        String status=attrService.saveSpuInfo(pmsProductInfo);
        return status;
    }

}
