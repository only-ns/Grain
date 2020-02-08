package com.only.grain.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.api.bean.PmsSearchSkuInfo;
import com.only.grain.api.bean.PmsSkuAttrValue;
import com.only.grain.api.bean.PmsSkuInfo;
import com.only.grain.api.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GrainSearchServiceApplicationTests {
    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;
    @Test
    public void contextLoads() throws IOException {
        //查询Mysql数据结构
        List<PmsSkuInfo> skuInfoList = skuService.getAllSku("61");
        //转换成es数据结构
        List<PmsSearchSkuInfo> searchSkuInfoList = new ArrayList<>();
        for (PmsSkuInfo skuInfo : skuInfoList) {
            PmsSearchSkuInfo searchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(skuInfo,searchSkuInfo);
            searchSkuInfoList.add(searchSkuInfo);
        }

        // 导入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo : searchSkuInfoList) {
            Index put = new Index.Builder(pmsSearchSkuInfo).index("grain").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
            DocumentResult execute = jestClient.execute(put);
        }

    }
    @Test
    public void query() throws IOException {
        ArrayList<PmsSearchSkuInfo> searchSkuInfoList = new ArrayList<>();
        //参数对象
        PmsSearchSkuInfo searchSkuInfo = new PmsSearchSkuInfo();
        searchSkuInfo.setSkuName("华为v30");
        ArrayList<PmsSkuAttrValue> attrValues = new ArrayList<>();
        PmsSkuAttrValue attrValue1 = new PmsSkuAttrValue();
        attrValue1.setValueId("50");
        PmsSkuAttrValue attrValue2 = new PmsSkuAttrValue();
        attrValue2.setValueId("53");
        attrValues.add(attrValue1);
        attrValues.add(attrValue2);
        searchSkuInfo.setSkuAttrValueList(attrValues);

        //dsl语句对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //先过滤后查询 --条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //过滤条件
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",searchSkuInfo.getSkuAttrValueList().get(0).getValueId());
        //查询条件
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", searchSkuInfo.getSkuName());
        //载入条件
        boolQueryBuilder.filter(termQueryBuilder).must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        searchSourceBuilder.from(0).size(20).highlight(null);
        //生成dsl语句
        String dslString = searchSourceBuilder.toString();
        System.out.println(dslString);

        Search search = new Search.Builder(dslString).addIndex("grain").addType("PmsSkuInfo").build();
        SearchResult execute = jestClient.execute(search);

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            searchSkuInfoList.add(hit.source);
        }

    }

}
