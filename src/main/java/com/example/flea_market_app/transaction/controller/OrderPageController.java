package com.example.flea_market_app.transaction.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderPageController {

	@GetMapping("/order/confirm")
	public String orderConfirm(Model model) {
		model.addAttribute("order", Map.of(
				"items", List.<Map<String, Object>>of(),
				"subtotal", "0",
				"shippingFee", "0",
				"total", "0"));
		model.addAttribute("payment", Map.of("method", "クレジットカード", "installment", "一括払い"));
		model.addAttribute("address", Map.of("name", "", "postcode", "", "fullAddress", ""));
		return "order/order_confirm";
	}
}
