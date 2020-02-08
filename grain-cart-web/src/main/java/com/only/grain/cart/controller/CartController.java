package com.only.grain.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.only.grain.annotations.LoginRequired;
import com.only.grain.api.bean.OmsCartItem;
import com.only.grain.api.bean.PmsSkuInfo;
import com.only.grain.api.bean.PmsSkuSaleAttrValue;
import com.only.grain.api.service.CartService;
import com.only.grain.api.service.SkuService;
import com.only.grain.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;

    /**
     * 同步购物车修改
     *
     * @param isChecked
     * @param skuId
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");

        // 调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);

        // 将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList", omsCartItems);
        return "cartListInner";
    }


    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        modelMap.put("nickname",nickname);
        //查询cookie
        String cartCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

        if (StringUtils.isNotBlank(cartCookie)) {
            omsCartItems = JSON.parseArray(cartCookie, OmsCartItem.class);
        }

        if (StringUtils.isNotBlank(memberId)) {
            // 已经登录
            //查询db
            List<OmsCartItem> omsCartItemDbs = cartService.cartList(memberId);
            //保存cookie到db
            boolean is_flushCheck=false;
            for (OmsCartItem cartItem : omsCartItems) {
                boolean exist = false;
                for (OmsCartItem omsCartItemDb : omsCartItemDbs) {
                    if (omsCartItemDb.getProductSkuId().equals(cartItem.getProductSkuId())) {
                        //同步cookie中存在相同商品
                        omsCartItemDb.setQuantity(omsCartItemDb.getQuantity().add(cartItem.getQuantity()));
                        cartService.updateCart(omsCartItemDb);
                        is_flushCheck=true;
                        exist = true;
                    }
                }
                //cookie中不相同的商品
                if (!exist) {
                    //添加 到数据库
                    is_flushCheck=true;
                    cartItem.setMemberId(memberId);
                    cartService.addCart(cartItem);
                    //添加到前端List
                    omsCartItemDbs.add(cartItem);
                }
            }
            cartService.flushCartCache(memberId);
            //删除cookie
            CookieUtil.setCookie(request, response, "cartListCookie", "", 0, true);
            omsCartItems=omsCartItemDbs;
        } else {
            //没有登录

        }
        //计算单个商品的总价
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList", omsCartItems);

        // 被勾选商品的总额
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }

    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, @RequestParam("num") int quantity, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        //查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        //封装信息到购物车
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setIsChecked("1");
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuService.getSkuSaleAttrValueListBySku(omsCartItem.getProductSkuId());
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            if (i == 0)
                omsCartItem.setSp1(skuSaleAttrValueList.get(0).getSaleAttrValueName());
            if (i == 1)
                omsCartItem.setSp2(skuSaleAttrValueList.get(1).getSaleAttrValueName());
            if (i == 2)
                omsCartItem.setSp3(skuSaleAttrValueList.get(2).getSaleAttrValueName());
        }

        //判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        if (StringUtils.isBlank(memberId)) {
            //没有登录

            // cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            List<OmsCartItem> cartItemList = new ArrayList<>();
            if (StringUtils.isBlank(cartListCookie)) {
                //cookie为空
                cartItemList.add(omsCartItem);
            } else {
                //cookie不为空
                cartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断是否存在这件商品
                boolean exist = if_cartItem_exist(cartItemList, omsCartItem);
                if (exist) {
                    //存在 商品数量叠加
                    for (OmsCartItem cartItem : cartItemList) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                } else {
                    //不存在 添加购物车
                    cartItemList.add(omsCartItem);
                }

            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartItemList), 60 * 60 * 24 * 3, true);
        } else {
            //已经登录
            // 从db中查出购物车数据
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId, skuId);

            if (omsCartItemFromDb == null) {
                // 该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname((String) request.getAttribute("nikename"));
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);

            } else {
                // 该用户添加过当前商品
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }

            // 同步缓存
            cartService.flushCartCache(memberId);

        }

        return "redirect:/success?skuId=" + skuId + "&memberId=" + memberId + "&num=" + quantity;
    }

    /**
     * 商品添加成功页
     *
     * @param memberId
     * @param skuId
     * @param num
     * @param modelMap
     * @return
     */
    @RequestMapping("success")
    @LoginRequired(loginSuccess = false)
    public String success(String memberId, String skuId, String num, ModelMap modelMap) {

        PmsSkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        modelMap.put("skuInfo", skuInfo);
        modelMap.put("skuNum", num);
        return "success";
    }

    private boolean if_cartItem_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {

        boolean b = false;

        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();

            if (productSkuId.equals(omsCartItem.getProductSkuId())) {
                b = true;
            }
        }


        return b;
    }
}
