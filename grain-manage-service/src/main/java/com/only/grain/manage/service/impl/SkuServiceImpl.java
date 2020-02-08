package com.only.grain.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.only.grain.api.bean.PmsSkuAttrValue;
import com.only.grain.api.bean.PmsSkuImage;
import com.only.grain.api.bean.PmsSkuInfo;
import com.only.grain.api.bean.PmsSkuSaleAttrValue;
import com.only.grain.api.service.SkuService;
import com.only.grain.manage.mapper.*;
import com.only.grain.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        if (pmsSkuInfo.getSkuDefaultImg()==null)
            try{
                pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
            }catch (Exception e){
                System.out.println("");
            }
        //保存销售商品Sku信息
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String saveId = pmsSkuInfo.getId();
        //保存销售商品平台属性值
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(saveId);
            pmsSkuAttrValueMapper.insertSelective(skuAttrValue);
        }
        //保存销售商品销售属性值
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(saveId);
            pmsSkuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }
        //保存销售商品图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(saveId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);

        }

        return "success";
    }


    public PmsSkuInfo getSkuInfoByIdFromDb(String skuId) {
        //skuInfo
        PmsSkuInfo pmsSkuInfo= pmsSkuInfoMapper.selectByPrimaryKey(skuId);
        if (pmsSkuInfo==null) return pmsSkuInfo;
        //sku Image;
        PmsSkuImage skuImage = new PmsSkuImage();
        skuImage.setSkuId(skuId);
        List<PmsSkuImage> imageList = pmsSkuImageMapper.select(skuImage);
        pmsSkuInfo.setSkuImageList(imageList);
        return pmsSkuInfo;
    }
    @Override
    public PmsSkuInfo getSkuInfoById(String skuId) {
        PmsSkuInfo pmsSkuInfo=new PmsSkuInfo();
        //连接缓存
        Jedis jedis=null;
        try {
            jedis = redisUtil.getJedis();
            String skuKey= "sku:"+skuId+":info";
            String skuLock= "sku:"+skuId+":lock";
            //查询缓存
            String skuJson = jedis.get(skuKey);

            if (StringUtils.isNotBlank(skuJson)){
                pmsSkuInfo=JSON.parseObject(skuJson,PmsSkuInfo.class);
            }else {
                //没有缓存 查询数据库
                //获得数据库锁 10秒后销毁
                String Ok = jedis.set(skuLock, "1", "nx", "px", 10 * 1000);
                if ("Ok".equalsIgnoreCase(Ok)){
                    //得到数据库锁 查询数据库
                    pmsSkuInfo= getSkuInfoByIdFromDb(skuId);
                    if (pmsSkuInfo!=null){
                        //将结果缓存到Redis
                        jedis.set(skuKey,JSON.toJSONString(pmsSkuInfo));
                    }else {
                        //数据库中不存在当前sku
                        //防止缓存穿透,将null或空字符串设置给Redis
                        jedis.setex(skuKey,30,JSON.toJSONString(""));
                    }
                    //释放数据库锁
                    jedis.del(skuId);
                }else {
                    //没有获得数据库锁 自旋(睡眠几秒后，重新访问本方法)
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return getSkuInfoById(skuId);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null)
                jedis.close();
        }
        return pmsSkuInfo;
    }


    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);

        return pmsSkuInfos;
    }

    /**
     * 查询某一分类下的全部销售商品
     * 用于建立elasticSearch商品搜索
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuSaleAttrValue> getSkuSaleAttrValueListBySku(String skuId) {
        PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
        pmsSkuSaleAttrValue.setSkuId(skuId);
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValueList = pmsSkuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);
        return pmsSkuSaleAttrValueList;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if (price.compareTo(skuInfo.getPrice())==0) {
            return  true;
        }
        return false;
    }
}
