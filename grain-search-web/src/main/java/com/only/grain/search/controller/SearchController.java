package com.only.grain.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.annotations.LoginRequired;
import com.only.grain.api.bean.*;
import com.only.grain.api.service.AttrService;
import com.only.grain.api.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(HttpServletRequest request ,ModelMap modelMap){
        modelMap.put("nickName",request.getAttribute("nickname"));
        return "index";
    }

    @RequestMapping("list.html")
    public String list_html(PmsSearchParam pmsSearchParam,ModelMap modelMap, HttpServletRequest request){
        // 调用搜索服务，返回搜索结果 (得到商品)
        List<PmsSearchSkuInfo> searchSkuInfoList = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",searchSkuInfoList);

        // 抽取检索结果包含的平台属性集合 (抽取商品的id)
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : searchSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        // 根据valueId将-属性列表-查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //获取当前urlParam
        String urlParam = request.getQueryString();
        try {
            urlParam=URLDecoder.decode(urlParam,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        modelMap.put("urlParam",urlParam);

        // 生成面包屑
        // 对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        if (delValueIds != null) {
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            //属性
            for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
                //属性值
                for (PmsBaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                    for (int i = 0; i < delValueIds.length; i++) {
                        if (delValueIds[i].equals(attrValue.getId())) {
                            PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                            pmsSearchCrumb.setValueId(attrValue.getId());
                            pmsSearchCrumb.setValueName(attrValue.getValueName());
                            pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(urlParam, delValueIds[i]));
                            pmsSearchCrumbs.add(pmsSearchCrumb);
                        }
                    }
                }
            }

            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }

        modelMap.put("keyword",pmsSearchParam.getKeyword());
        modelMap.put("catalog3Id",pmsSearchParam.getCatalog3Id());
        return "list";
    }

    private String getUrlParamForCrumb(String url, String delValueId) {
       String delParam="&valueId="+delValueId;
       String urlParam=url.replace(delParam,"");
        return urlParam;
    }
}
