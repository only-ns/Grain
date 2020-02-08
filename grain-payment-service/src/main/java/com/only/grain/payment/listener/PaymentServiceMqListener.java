package com.only.grain.payment.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeAppMergePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.only.grain.api.bean.PaymentInfo;
import com.only.grain.api.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_CHECK_QUEUE")
    public void checkPaymentResult(MapMessage mapMessage) throws JMSException {
        String orderSn = mapMessage.getString("orderSn");

        int count = mapMessage.getInt("count");
        if (count<=0)
            return;
        Boolean result = paymentService.checkPaymentResult(orderSn);
        if (result){
            //订单已支付
            return;
        }else {
            //订单未支付
            paymentService.sendDelayCheckPaymentQueue(orderSn,count--);
        }

    }
}
