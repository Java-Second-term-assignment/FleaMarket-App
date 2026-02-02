package com.example.flea_market_app.integration.payment.dto;

public record PaymentRequest(
        long amount,          // 最小通貨単位
        String currency,       // "JPY"
        String orderId,        // 自ドメインID（外部に metadata で埋めても良い）
        String customerId      // 将来用。不要ならnullでOK
) {}
