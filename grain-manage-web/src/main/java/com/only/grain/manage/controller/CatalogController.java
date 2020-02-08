package com.only.grain.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.api.bean.PmsBaseCatalog1;
import com.only.grain.api.bean.PmsBaseCatalog2;
import com.only.grain.api.bean.PmsBaseCatalog3;
import com.only.grain.api.service.CatalogService;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class CatalogController {
    @Reference
    private CatalogService catalogService;
    @RequestMapping(value = "/getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        return catalogService.getCatalog1();
    }

    @RequestMapping(value = "/getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){
        return catalogService.getCatalog2(catalog1Id);
    }
    @RequestMapping(value = "/getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
        return catalogService.getCatalog3(catalog2Id);
    }
}
