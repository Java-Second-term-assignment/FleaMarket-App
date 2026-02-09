package com.example.flea_market_app.transaction.controller;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.config.payment.StripeProperties;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.domain.ReviewRating;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.repository.UserRepository;
import com.example.flea_market_app.user.service.UserRankService;
import com.example.flea_market_app.user.service.UserService;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.integration.payment.dto.PaymentResult;
import com.example.flea_market_app.transaction.service.OrderQueryService;
import com.example.flea_market_app.transaction.service.OrderService;
import com.example.flea_market_app.transaction.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderPageController {

	private static final String ORDER_PAYMENT = "orderPayment";
	private static final String ORDER_ADDRESS = "orderAddress";
	private static final String STRIPE_PAYMENT_ORDER_ID = "stripePaymentOrderId";
	private static final String STRIPE_PAYMENT_CLIENT_SECRET = "stripePaymentClientSecret";

	private static final Map<String, String> DEFAULT_PAYMENT = Map.of("method", "クレジットカード", "installment", "一括払い");
	private static final Map<String, String> DEFAULT_ADDRESS = Map.of("name", "", "postcode", "", "fullAddress", "");

	private final ProductListService productListService;
	private final OrderQueryService orderQueryService;
	private final OrderService orderService;
	private final OrderRepository orderRepository;
	private final ReviewService reviewService;
	private final UserService userService;
	private final UserRepository userRepository;
	private final UserRankService userRankService;
	private final MessageSource messageSource;
	private final StripeProperties stripeProperties;

	@PostMapping("/order/confirm")
	public String orderConfirmPost(
			@RequestParam(required = false) UUID productId,
			HttpSession session,
			RedirectAttributes ra,
			Locale locale) {
		if (productId == null) {
			ra.addFlashAttribute("errorMessage", "商品を指定してください。");
			return "redirect:/order/confirm";
		}
		var userId = SecurityUtil.getCurrentUserId();
		@SuppressWarnings("unchecked")
		Map<String, String> sessionAddress = (Map<String, String>) session.getAttribute(ORDER_ADDRESS);
		Map<String, String> addressMap = sessionAddress != null ? sessionAddress : DEFAULT_ADDRESS;
		String addressSnapshot = toJsonSnapshot(addressMap);

		boolean stripeEnabled = stripeProperties.apiKey() != null && !stripeProperties.apiKey().isBlank();
		if (stripeEnabled) {
			try {
				UUID orderId = orderService.createOrderFromProductPending(productId, userId, addressSnapshot);
				String currency = stripeProperties.currency() != null && !stripeProperties.currency().isBlank()
						? stripeProperties.currency() : "JPY";
				String clientSecret = orderService.createPaymentIntentForOrder(orderId, currency);
				session.setAttribute(STRIPE_PAYMENT_ORDER_ID, orderId.toString());
				session.setAttribute(STRIPE_PAYMENT_CLIENT_SECRET, clientSecret);
				return "redirect:/order/payment?orderId=" + orderId;
			} catch (ValidationBusinessException e) {
				String message = messageSource.getMessage(e.getMessageKey(), null, "この商品はすでに注文済みです。", locale != null ? locale : Locale.getDefault());
				ra.addFlashAttribute("errorMessage", message);
				return "redirect:/order/confirm?productId=" + productId;
			} catch (DataIntegrityViolationException e) {
				ra.addFlashAttribute("errorMessage", messageSource.getMessage("error.order.item_already_ordered", null, "この商品はすでに注文済みです。", locale != null ? locale : Locale.getDefault()));
				return "redirect:/order/confirm?productId=" + productId;
			}
		}

		try {
			UUID orderId = orderService.createOrderFromProduct(productId, userId, addressSnapshot);
			ra.addFlashAttribute("message", "注文を作成しました。");
			return "redirect:/user/orders/" + orderId;
		} catch (ValidationBusinessException e) {
			String message = messageSource.getMessage(e.getMessageKey(), null, "この商品はすでに注文済みです。", locale != null ? locale : Locale.getDefault());
			ra.addFlashAttribute("errorMessage", message);
			return "redirect:/order/confirm?productId=" + productId;
		} catch (DataIntegrityViolationException e) {
			ra.addFlashAttribute("errorMessage", messageSource.getMessage("error.order.item_already_ordered", null, "この商品はすでに注文済みです。", locale != null ? locale : Locale.getDefault()));
			return "redirect:/order/confirm?productId=" + productId;
		}
	}

	@GetMapping("/order/confirm")
	public String orderConfirm(
			@RequestParam(required = false) UUID productId,
			Model model,
			HttpSession session,
			Locale locale) {
		if (productId != null) {
			productListService.getProductDetail(productId).ifPresent(product -> {
				Long price = product.getPrice() != null ? product.getPrice() : 0L;
				long commissionFee = 0L;
				if (product.getSellerId() != null) {
					UserEntity seller = userRepository.findById(product.getSellerId()).orElse(null);
					if (seller != null) {
						UserRank rank = userRankService.loadRank(seller.getUserRankId());
						commissionFee = price * rank.getCommissionBps() / 10000;
					}
				}
				List<Map<String, Object>> items = List.of(Map.<String, Object>of(
						"imageUrl", product.getMainImageUrl() != null ? product.getMainImageUrl() : "",
						"name", product.getName() != null ? product.getName() : "",
						"price", price,
						"quantity", 1));
				model.addAttribute("order", Map.<String, Object>of(
						"items", items,
						"subtotal", String.valueOf(price),
						"shippingFee", "0",
						"commissionFee", Long.valueOf(commissionFee),
						"total", String.valueOf(price)));
			});
		}
		if (!model.containsAttribute("order")) {
			model.addAttribute("order", Map.<String, Object>of(
					"items", List.<Map<String, Object>>of(),
					"subtotal", "0",
					"shippingFee", "0",
					"commissionFee", Long.valueOf(0L),
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

		boolean stripeEnabled = stripeProperties.apiKey() != null && !stripeProperties.apiKey().isBlank();
		if (stripeEnabled && productId != null) {
			try {
				String addressSnapshot = toJsonSnapshot(addressForView);
				PaymentResult paymentResult = orderService.createPaymentIntentForCheckout(productId, userId, addressSnapshot);
				model.addAttribute("clientSecret", paymentResult.clientSecret());
				model.addAttribute("paymentIntentId", paymentResult.externalPaymentId());
				model.addAttribute("publishableKey", stripeProperties.publishableKey());
			} catch (ValidationBusinessException e) {
				model.addAttribute("errorMessage", messageSource.getMessage(e.getMessageKey(), null, "この商品は注文できません。", locale != null ? locale : Locale.getDefault()));
			}
		}

		return "order/order_confirm";
	}

	@GetMapping("/order/complete-purchase")
	public String completePurchase(
			@RequestParam(name = "payment_intent_id") String paymentIntentId,
			RedirectAttributes ra) {
		if (paymentIntentId == null || paymentIntentId.isBlank()) {
			ra.addFlashAttribute("errorMessage", "決済情報がありません。");
			return "redirect:/user/orders";
		}
		var userId = SecurityUtil.getCurrentUserId();
		try {
			UUID orderId = orderService.createOrderFromPaymentIntentMetadata(paymentIntentId);
			OrderEntity order = orderRepository.findById(orderId).orElse(null);
			if (order != null && !order.getBuyerId().equals(userId)) {
				ra.addFlashAttribute("errorMessage", "この決済は別のユーザーのものです。");
				return "redirect:/user/orders";
			}
			ra.addFlashAttribute("message", "決済が完了しました。");
			return "redirect:/user/orders/" + orderId;
		} catch (ValidationBusinessException e) {
			ra.addFlashAttribute("errorMessage", messageSource.getMessage(e.getMessageKey(), null, "注文の作成に失敗しました。", Locale.getDefault()));
			return "redirect:/user/orders";
		}
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

	/**
	 * Stripe 決済実行ページ。注文確定（Stripe 有効時）のリダイレクト先。セッションの client_secret と orderId を検証して表示する。
	 */
	@GetMapping("/order/payment")
	public String orderPaymentPage(
			@RequestParam UUID orderId,
			HttpSession session,
			Model model,
			RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		Object sessionOrderId = session.getAttribute(STRIPE_PAYMENT_ORDER_ID);
		Object sessionClientSecret = session.getAttribute(STRIPE_PAYMENT_CLIENT_SECRET);
		if (sessionOrderId == null || sessionClientSecret == null
				|| !orderId.toString().equals(sessionOrderId.toString())) {
			ra.addFlashAttribute("errorMessage", "決済セッションが無効です。注文確認からやり直してください。");
			return "redirect:/order/confirm";
		}
		if (stripeProperties.publishableKey() == null || stripeProperties.publishableKey().isBlank()) {
			ra.addFlashAttribute("errorMessage", "決済の設定が完了していません。");
			return "redirect:/user/orders/" + orderId;
		}
		OrderEntity order = orderRepository.findById(orderId).orElse(null);
		if (order == null || !order.getBuyerId().equals(userId)
				|| !OrderStatus.PENDING.name().equals(order.getStatus())) {
			ra.addFlashAttribute("errorMessage", "この注文の支払いを実行できません。");
			return "redirect:/user/orders";
		}
		model.addAttribute("orderId", orderId.toString());
		model.addAttribute("clientSecret", sessionClientSecret.toString());
		model.addAttribute("publishableKey", stripeProperties.publishableKey());
		model.addAttribute("itemId", order.getItemId() != null ? order.getItemId().toString() : null);
		model.addAttribute("orderDetail", orderQueryService.getOrderDetail(orderId, userId));
		return "order/stripe_payment";
	}

	@GetMapping("/address")
	public String address(
			@RequestParam(required = false) UUID productId,
			@RequestParam(required = false) String returnTo,
			Model model, HttpSession session) {
		if (productId != null) {
			model.addAttribute("productId", productId);
		}
		model.addAttribute("returnTo", returnTo);
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
			@RequestParam(required = false) String returnTo,
			HttpSession session) {
		session.setAttribute(ORDER_ADDRESS, Map.of("name", name, "postcode", postcode, "fullAddress", fullAddress));
		if ("cart".equals(returnTo)) {
			return "redirect:/cart";
		}
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
	public String orderDetail(@PathVariable("id") UUID orderId, Model model, HttpSession session) {
		var userId = SecurityUtil.getCurrentUserId();
		Object sessionOrderId = session.getAttribute(STRIPE_PAYMENT_ORDER_ID);
		if (sessionOrderId != null && orderId.toString().equals(sessionOrderId.toString())) {
			session.removeAttribute(STRIPE_PAYMENT_ORDER_ID);
			session.removeAttribute(STRIPE_PAYMENT_CLIENT_SECRET);
		}
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
