package com.example.flea_market_app.transaction.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.flea_market_app.catalog.service.ProductListService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderPageController {

	private final ProductListService productListService;

	@GetMapping("/order/confirm")
	public String orderConfirm(@RequestParam(required = false) UUID productId, Model model) {
		if (productId != null) {
			productListService.getProductDetail(productId).ifPresent(product -> {
				Long price = product.getPrice() != null ? product.getPrice() : 0L;
				List<Map<String, Object>> items = List.of(Map.<String, Object>of(
						"imageUrl", product.getMainImageUrl() != null ? product.getMainImageUrl() : "",
						"name", product.getName() != null ? product.getName() : "",
						"price", price,
						"quantity", 1));
				model.addAttribute("order", Map.<String, Object>of(
						"items", items,
						"subtotal", String.valueOf(price),
						"shippingFee", "0",
						"total", String.valueOf(price)));
			});
		}
		if (!model.containsAttribute("order")) {
			model.addAttribute("order", Map.<String, Object>of(
					"items", List.<Map<String, Object>>of(),
					"subtotal", "0",
					"shippingFee", "0",
					"total", "0"));
		}
		model.addAttribute("payment", Map.of("method", "クレジットカード", "installment", "一括払い"));
		model.addAttribute("address", Map.of("name", "", "postcode", "", "fullAddress", ""));
		return "order/order_confirm";
	}
}
