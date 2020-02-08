package com.only.grain.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.only.grain.api.bean.PaymentInfo;
import com.only.grain.api.service.PaymentService;
import com.only.grain.mq.ActiveMQUtil;
import com.only.grain.payment.mapper.PaymentMapper;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            try{
                paymentMapper.updateByExampleSelective(paymentInfo,example);
                // 支付成功后，引起的系统服务-》订单服务的更新-》库存服务-》物流服务
                // 调用mq发送支付成功的消息
                Queue payhment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payhment_success_queue);

                //TextMessage textMessage=new ActiveMQTextMessage();//字符串文本

                MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

                mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());

                producer.send(mapMessage);

                session.commit();
            }catch (Exception ex){
                // 消息回滚
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
        return paymentInfo;
    }

    @Override
    public boolean checkPaymentResult(String orderSn) {

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> pramMap = new HashMap<>();
        pramMap.put("out_trade_no", orderSn);
        request.setBizContent(pramMap.toString());

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                System.out.println("调用成功");
                /*
                验签
                AlipaySignature.rsaCheckV1()
                */
                if ("trade_status".equals(response.getTradeStatus()))
                {
                    //支付成功 更新支付信息
                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setOrderSn(response.getOutTradeNo());
                    paymentInfo.setPaymentStatus("已支付");
                    paymentInfo.setAlipayTradeNo(response.getTradeNo());// 支付宝的交易凭证号
                    paymentInfo.setSubject(response.getSubject());
                    paymentInfo.setCallbackTime(new Date());
                    // 更新用户的支付状态
                    updatePayment(paymentInfo);
                    return true;
                }
            } else {
                System.out.println("调用失败");
            }
        }catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 生产者,发送支付结果检查队列
     * @param orderSn 外部订单号
     * @param count   剩余检查次数
     */
    @Override
    public void sendDelayCheckPaymentQueue(String orderSn, int count) {
        Connection connection=null;
        Session session=null;
        if (count<=0)
            return;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            try {
                Queue queue = session.createQueue("PAYMENT_CHECK_QUEUE");
                MessageProducer messageProducer = session.createProducer(queue);
                ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
                mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*60);
                mapMessage.setString("orderSn",orderSn);
                mapMessage.setInt("count",count);
                messageProducer.send(mapMessage);
                session.commit();
            }catch (Exception e){
                e.printStackTrace();
                try {
                    if (session!=null)
                        session.rollback();
                }catch (Exception e1){
                    e1.printStackTrace();
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
            try {
                if (connection!=null)
                    connection.close();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {

        }

    }
}
