package com.example.flea_market_app.transaction.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.transaction.domain.ReviewRating;
import com.example.flea_market_app.user.service.UserService;
import com.example.flea_market_app.transaction.service.OrderQueryService;
import com.example.flea_market_app.transaction.service.OrderService;
import com.example.flea_market_app.transaction.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderPageController {

	private static final String ORDER_PAYMENT = "orderPayment";
	private static final String ORDER_ADDRESS = "orderAddress";

	private static final Map<String, String> DEFAULT_PAYMENT = Map.of("method", "クレジットカード", "installment", "一括払い");
	private static final Map<String, String> DEFAULT_ADDRESS = Map.of("name", "", "postcode", "", "fullAddress", "");

	private final ProductListService productListService;
	private final OrderQueryService orderQueryService;
	private final OrderService orderService;
	private final ReviewService reviewService;
	private final UserService userService;

	@PostMapping("/order/confirm")
	public String orderConfirmPost(
			@RequestParam(required = false) UUID productId,
			HttpSession session,
			RedirectAttributes ra) {
		if (productId == null) {
			ra.addFlashAttribute("errorMessage", "商品を指定してください。");
			return "redirect:/order/confirm";
		}
		var userId = SecurityUtil.getCurrentUserId();
		@SuppressWarnings("unchecked")
		Map<String, String> sessionAddress = (Map<String, String>) session.getAttribute(ORDER_ADDRESS);
		Map<String, String> addressMap = sessionAddress != null ? sessionAddress : DEFAULT_ADDRESS;
		String addressSnapshot = toJsonSnapshot(addressMap);
		UUID orderId = orderService.createOrderFromProduct(productId, userId, addressSnapshot);
		ra.addFlashAttribute("message", "注文を作成しました。");
		return "redirect:/user/orders/" + orderId;
	}

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
		var userId = SecurityUtil.getCurrentUserId();
		Map<String, String> addressForView = sessionAddress != null
				? sessionAddress
				: userService.getDefaultShippingAddress(userId);
		model.addAttribute("address", addressForView);
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
		var userId = SecurityUtil.getCurrentUserId();
		Map<String, String> addressForView = sessionAddress != null
				? sessionAddress
				: userService.getDefaultShippingAddress(userId);
		model.addAttribute("address", addressForView);
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

	@GetMapping("/user/orders")
	public String orderList(
			@RequestParam(required = false, defaultValue = "buyer") String scope,
			Model model) {
		if (!"seller".equalsIgnoreCase(scope)) {
			scope = "buyer";
		}
		var userId = SecurityUtil.getCurrentUserId();
		model.addAttribute("orders", orderQueryService.getMyOrders(userId, scope));
		model.addAttribute("scope", scope);
		return "order/list";
	}

	@GetMapping("/user/orders/{id}")
	public String orderDetail(@PathVariable("id") UUID orderId, Model model) {
		var userId = SecurityUtil.getCurrentUserId();
		model.addAttribute("order", orderQueryService.getOrderDetail(orderId, userId));
		model.addAttribute("currentUserId", userId);
		return "order/detail";
	}

	@PostMapping("/user/orders/{id}/confirm")
	public String orderConfirmAction(@PathVariable("id") UUID orderId, RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		orderService.confirmPurchase(orderId, userId);
		ra.addFlashAttribute("message", "購入を確定しました。");
		return "redirect:/user/orders/" + orderId;
	}

	@PostMapping("/user/orders/{id}/shipment")
	public String orderShipmentAction(@PathVariable("id") UUID orderId, RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		orderService.notifyShipment(orderId, userId);
		ra.addFlashAttribute("message", "発送通知を送りました。");
		return "redirect:/user/orders/" + orderId;
	}

	@PostMapping("/user/orders/{id}/receipt")
	public String orderReceiptAction(@PathVariable("id") UUID orderId, RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		orderService.confirmReceipt(orderId, userId);
		ra.addFlashAttribute("message", "受取確認しました。取引が完了しました。");
		return "redirect:/user/orders/" + orderId;
	}

	@PostMapping("/user/orders/{id}/cancel")
	public String orderCancelAction(@PathVariable("id") UUID orderId,
			@RequestParam("reason") String reason, RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		orderService.cancel(orderId, userId, reason != null ? reason : "");
		ra.addFlashAttribute("message", "取引をキャンセルしました。");
		return "redirect:/user/orders/" + orderId;
	}

	@PostMapping("/user/orders/{id}/review")
	public String orderReviewAction(@PathVariable("id") UUID orderId,
			@RequestParam("rating") String rating,
			@RequestParam(value = "comment", required = false) String comment, RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		var ratingEnum = "BAD".equalsIgnoreCase(rating) ? ReviewRating.BAD : ReviewRating.GOOD;
		reviewService.submitReview(orderId, userId, ratingEnum, comment != null ? comment : "");
		ra.addFlashAttribute("message", "評価を送りました。");
		return "redirect:/user/orders/" + orderId;
	}

	private static String toJsonSnapshot(Map<String, String> map) {
		String name = escapeJson(map.getOrDefault("name", ""));
		String postcode = escapeJson(map.getOrDefault("postcode", ""));
		String fullAddress = escapeJson(map.getOrDefault("fullAddress", ""));
		return "{\"name\":\"" + name + "\",\"postcode\":\"" + postcode + "\",\"fullAddress\":\"" + fullAddress + "\"}";
	}

	private static String escapeJson(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
