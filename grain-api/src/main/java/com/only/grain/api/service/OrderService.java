package com.only.grain.api.service;

import com.only.grain.api.bean.OmsOrder;

public interface OrderService {
    String genTradeCode(String memberId);

    String checkTradeCode(String memberId, String tradeCode);

    String saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByMemberId(String memberId,String orderId);

    void updateOrder(OmsOrder omsOrder);
}
