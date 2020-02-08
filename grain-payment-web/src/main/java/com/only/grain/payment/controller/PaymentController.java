package com.only.grain.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.only.grain.annotations.LoginRequired;
import com.only.grain.api.bean.OmsOrder;
import com.only.grain.api.bean.PaymentInfo;
import com.only.grain.api.service.OrderService;
import com.only.grain.api.service.PaymentService;
import com.only.grain.payment.config.AlipayConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Reference
    OrderService orderService;
    @Reference
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;
    @RequestMapping("mx/submit")
    @LoginRequired
    public String mxPay(String orderId,HttpServletRequest request, ModelMap modelMap){
        return null;
    }
    @RequestMapping("alipay/submit")
    @LoginRequired
    @ResponseBody
    public String aliPay(String orderId, HttpServletRequest request) throws AlipayApiException, IOException {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        OmsOrder omsOrder = orderService.getOrderByMemberId(memberId, orderId);
        String totalAmount = omsOrder.getTotalAmount().toString();
        String  orderSn= omsOrder.getOrderSn();

        //请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String, String> map = new HashMap<>();
        map.put("out_trade_no",orderSn);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",totalAmount);
        map.put("subject","华为 HUAWEI P30 超感光徕卡三摄麒麟980AI智能芯片全面屏屏内指纹版手机");
        alipayRequest.setBizContent(JSON.toJSONString(map));
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(form);
        // 生成并且保存用户的支付信息

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(omsOrder.getOrderSn());
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("谷粒商城商品一件");
        paymentInfo.setTotalAmount(new BigDecimal(totalAmount));
        paymentService.savePaymentInfo(paymentInfo);

        //发送延迟队列检查支付结果
        paymentService.sendDelayCheckPaymentQueue(orderSn,5);

        return form;
    }
    @RequestMapping("alipay/callback")
    public String ailplay_callback(HttpServletRequest request,ModelMap modelMap) {
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String total_amount = request.getParameter("total_amount");
        String trade_status = request.getParameter("trade_status");
        String subject = request.getParameter("subject");
        String call_back_content = request.getParameter("call_back_content");

        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)) {
            // 验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setSubject(subject);
            paymentInfo.setCallbackTime(new Date());
            // 更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
            return "finish";

        }
        return "";

    }

    @RequestMapping("index")
    @LoginRequired
    public String index(String orderId,HttpServletRequest request, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        modelMap.put("memberId",memberId);
        modelMap.put("nickname",nickname);
        modelMap.put("orderId",orderId);
        OmsOrder omsOrder = orderService.getOrderByMemberId(memberId, orderId);
        if (omsOrder!=null){
            String totalAmount = omsOrder.getTotalAmount().toString();
            String  orderSn= omsOrder.getOrderSn();
            modelMap.put("totalAmount",totalAmount);
            modelMap.put("orderSn",orderSn);
        }


        return "index";
    }
}
