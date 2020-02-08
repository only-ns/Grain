package com.only.grain.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.only.grain.api.bean.OmsOrder;
import com.only.grain.api.bean.OmsOrderItem;
import com.only.grain.api.service.OrderService;
import com.only.grain.mq.ActiveMQUtil;
import com.only.grain.order.mapper.OmsOrderItemMapper;
import com.only.grain.order.mapper.OmsOrderMapper;
import com.only.grain.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:" + memberId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        try {
            jedis.setex(tradeKey, 60 * 30, tradeCode);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:" + memberId + ":tradeCode";
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));

            if (eval != null && eval != 0) {
                // jedis.del(tradeKey);
                return "success";
            } else {
                return "fail";
            }
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    /**
     * 保存订单
     *
     * @param omsOrder
     * @return
     */
    @Override
    public String saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        for (OmsOrderItem omsOrderItem : omsOrder.getOmsOrderItemList()) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
        return orderId;
    }

    @Override
    public OmsOrder getOrderByMemberId(String memberId, String orderId) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setMemberId(memberId);
        omsOrder.setId(orderId);
        OmsOrder omsOrderOne = omsOrderMapper.selectOne(omsOrder);
        return omsOrderOne;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());

        // 发送一个订单已支付的队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);
            TextMessage textMessage = new ActiveMQTextMessage();//字符串文本
            //MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

            // 查询订单的对象，转化成json字符串，存入ORDER_PAY_QUEUE的消息队列
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItemParam);
            omsOrderResponse.setOmsOrderItems(select);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));

            omsOrderMapper.updateByExampleSelective(omsOrder, example);
            producer.send(textMessage);
            session.commit();
        } catch (Exception ex) {
            // 消息回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }

    }


}

