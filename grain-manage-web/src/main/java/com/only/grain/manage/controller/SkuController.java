package com.only.grain.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.api.bean.PmsSkuInfo;
import com.only.grain.api.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class SkuController {
    @Reference
    private SkuService skuService;

    @RequestMapping(value = "saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        String struts=skuService.saveSkuInfo(pmsSkuInfo);
        return struts;
    }

}
