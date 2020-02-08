package com.only.grain.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.api.bean.PmsProductImage;
import com.only.grain.api.bean.PmsProductInfo;
import com.only.grain.api.service.SpuService;
import com.only.grain.manage.util.PmsUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {
    @Reference
    private SpuService spuService;

    @RequestMapping(value = "/fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        String imageURL = PmsUploadUtil.uploadImage(multipartFile);
        return imageURL;
    }

    @RequestMapping(value = "spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> spuList = spuService.spuList(catalog3Id);
        return  spuList;
    }
    @RequestMapping(value = "spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        List<PmsProductImage> imageList = spuService.spuImageList(spuId);
        return  imageList;
    }

}
