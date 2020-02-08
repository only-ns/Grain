package com.only.grain.api.service;

import com.only.grain.api.bean.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo updatePayment(PaymentInfo paymentInfo);

    boolean checkPaymentResult(String orderSn);

    void sendDelayCheckPaymentQueue(String orderSn, int count);
}
