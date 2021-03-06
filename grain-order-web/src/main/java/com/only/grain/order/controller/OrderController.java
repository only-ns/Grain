package com.only.grain.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.annotations.LoginRequired;
import com.only.grain.api.bean.OmsCartItem;
import com.only.grain.api.bean.OmsOrder;
import com.only.grain.api.bean.OmsOrderItem;
import com.only.grain.api.bean.UmsMemberReceiveAddress;
import com.only.grain.api.service.CartService;
import com.only.grain.api.service.OrderService;
import com.only.grain.api.service.SkuService;
import com.only.grain.api.service.UserServer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    CartService cartService;
    @Reference
    UserServer userServer;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;

    /**
     * 提交订单
     * @param receiveAddressId
     * @param tradeCode
     * @param totalAmount
     * @param request
     * @param modelAndView
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, String tradeCode, BigDecimal totalAmount, HttpServletRequest request,ModelAndView modelAndView) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        // 检查交易码
        String success = orderService.checkTradeCode(memberId, tradeCode);

        if (success.equals("success")) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            // 订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount(); 运费，支付后，在生成物流信息时
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快点发货");
            String outTradeNo = "grain";
            outTradeNo = outTradeNo + System.currentTimeMillis();// 将毫秒时间戳拼接到外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + sdf.format(new Date());// 将时间字符串拼接到外部订单号

            omsOrder.setOrderSn(outTradeNo);//外部订单号
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userServer.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            // 当前日期加一天，一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            Date time = c.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus("0");
            omsOrder.setOrderType(0);
            omsOrder.setTotalAmount(totalAmount);

            // 根据用户id获得要购买的商品列表(购物车)，和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    // 获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    // 检价
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (b == false) {
                        //价格不一致 失败
                        modelAndView.setViewName("tradeFail");
                        return modelAndView;
                    }
                    // 验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());

                    omsOrderItem.setOrderSn(outTradeNo);// 外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");// 在实际仓库中的skuId

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItemList(omsOrderItems);

            // 将订单和订单详情写入数据库
            // 删除购物车的对应商品
            String orderId = orderService.saveOrder(omsOrder);


            // 重定向到支付系统
            modelAndView.setViewName("redirect:http://payment.grain.com:8087/index?orderId="+orderId);
            return modelAndView;
        } else {
            //下单失败!
            modelAndView.setViewName("tradeFail");
            return modelAndView;
        }



    }
    //去结算
    @RequestMapping("orderList")
    @LoginRequired(loginSuccess = true)
    public String orderList(HttpServletRequest request,ModelMap modelMap) {

        return "list";
    }
//去结算
    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //查询收货地址
        List<UmsMemberReceiveAddress> receiveAddressList = userServer.getReceiveAddressByUserId(memberId);

        List<OmsCartItem> cartItemList = cartService.cartList(memberId);

        List<OmsOrderItem> orderItemList = new ArrayList<>();
        for (OmsCartItem omsCartItem : cartItemList) {
            if ("1".equals(omsCartItem.getIsChecked())) {
                OmsOrderItem orderItem = new OmsOrderItem();
                orderItem.setProductId(omsCartItem.getProductId());
                orderItem.setSp1(omsCartItem.getSp1());
                orderItem.setSp2(omsCartItem.getSp2());
                orderItem.setSp3(omsCartItem.getSp3());
                orderItem.setProductSkuId(omsCartItem.getProductSkuId());
                orderItem.setProductQuantity(omsCartItem.getQuantity());
                orderItem.setRealAmount(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                orderItem.setProductName(omsCartItem.getProductName());
                orderItem.setProductPic(omsCartItem.getProductPic());
                orderItemList.add(orderItem);
            }
        }
        modelMap.put("receiveAddressList",receiveAddressList);
        modelMap.put("orderItemList", orderItemList);
        modelMap.put("nickName",request.getAttribute("nickName"));
        modelMap.put("totalAmount", getTotalAmount(cartItemList));

        // 生成交易码，为了在提交订单时做交易码的校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
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
}
