package com.example.flea_market_app.transaction.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.flea_market_app.catalog.service.ProductListService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderPageController {

	private static final String ORDER_PAYMENT = "orderPayment";
	private static final String ORDER_ADDRESS = "orderAddress";

	private static final Map<String, String> DEFAULT_PAYMENT = Map.of("method", "クレジットカード", "installment", "一括払い");
	private static final Map<String, String> DEFAULT_ADDRESS = Map.of("name", "", "postcode", "", "fullAddress", "");

	private final ProductListService productListService;

	@GetMapping("/order/confirm")
	public String orderConfirm(@RequestParam(required = false) UUID productId, Model model, HttpSession session) {
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
		if (productId != null) {
			model.addAttribute("productId", productId);
		}
		@SuppressWarnings("unchecked")
		Map<String, String> sessionPayment = (Map<String, String>) session.getAttribute(ORDER_PAYMENT);
		model.addAttribute("payment", sessionPayment != null ? sessionPayment : DEFAULT_PAYMENT);
		@SuppressWarnings("unchecked")
		Map<String, String> sessionAddress = (Map<String, String>) session.getAttribute(ORDER_ADDRESS);
		model.addAttribute("address", sessionAddress != null ? sessionAddress : DEFAULT_ADDRESS);
		return "order/order_confirm";
	}

	@GetMapping("/payment")
	public String payment(@RequestParam(required = false) UUID productId, Model model, HttpSession session) {
		if (productId != null) {
			model.addAttribute("productId", productId);
		}
		@SuppressWarnings("unchecked")
		Map<String, String> sessionPayment = (Map<String, String>) session.getAttribute(ORDER_PAYMENT);
		model.addAttribute("payment", sessionPayment != null ? sessionPayment : DEFAULT_PAYMENT);
		return "order/payment";
	}

	@PostMapping("/payment")
	public String paymentPost(
			@RequestParam String method,
			@RequestParam String installment,
			@RequestParam(required = false) UUID productId,
			HttpSession session) {
		session.setAttribute(ORDER_PAYMENT, Map.of("method", method, "installment", installment));
		return productId != null ? "redirect:/order/confirm?productId=" + productId : "redirect:/order/confirm";
	}

	@GetMapping("/address")
	public String address(@RequestParam(required = false) UUID productId, Model model, HttpSession session) {
		if (productId != null) {
			model.addAttribute("productId", productId);
		}
		@SuppressWarnings("unchecked")
		Map<String, String> sessionAddress = (Map<String, String>) session.getAttribute(ORDER_ADDRESS);
		model.addAttribute("address", sessionAddress != null ? sessionAddress : DEFAULT_ADDRESS);
		return "order/address";
	}

	@PostMapping("/address")
	public String addressPost(
			@RequestParam String name,
			@RequestParam String postcode,
			@RequestParam String fullAddress,
			@RequestParam(required = false) UUID productId,
			HttpSession session) {
		session.setAttribute(ORDER_ADDRESS, Map.of("name", name, "postcode", postcode, "fullAddress", fullAddress));
		return productId != null ? "redirect:/order/confirm?productId=" + productId : "redirect:/order/confirm";
	}
}
