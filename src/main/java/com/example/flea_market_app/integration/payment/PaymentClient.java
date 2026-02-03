package com.example.flea_market_app.integration.payment;

import com.example.flea_market_app.integration.payment.dto.PaymentRequest;
import com.example.flea_market_app.integration.payment.dto.PaymentResult;

public interface PaymentClient {
	PaymentResult charge(PaymentRequest request);

	PaymentResult refund(String externalPaymentId, long amount);
}
