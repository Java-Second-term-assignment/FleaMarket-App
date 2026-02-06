package com.example.flea_market_app.transaction.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.transaction.service.CartService;
import com.example.flea_market_app.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartPageController {

	private static final String ORDER_ADDRESS = "orderAddress";

	private static final Map<String, String> DEFAULT_ADDRESS = Map.of("name", "", "postcode", "", "fullAddress", "");

	private final CartService cartService;
	private final ProductListService productListService;
	private final UserService userService;

	@PostMapping("/cart/add")
	public String add(
			@RequestParam("productId") UUID productId,
			HttpSession session,
			RedirectAttributes ra) {
		cartService.addItem(session, productId);
		ra.addFlashAttribute("message", "カートに追加しました。");
		return "redirect:/cart";
	}

	@GetMapping("/cart")
	public String view(HttpSession session, Model model) {
		List<UUID> itemIds = cartService.getItemIds(session);

		List<Map<String, Object>> items = new ArrayList<>();
		long subtotal = 0L;
		for (UUID itemId : itemIds) {
			productListService.getProductDetail(itemId).ifPresent(product -> {
				Long price = product.getPrice() != null ? product.getPrice() : 0L;
				items.add(Map.<String, Object>of(
						"id", product.getId(),
						"imageUrl", product.getMainImageUrl() != null ? product.getMainImageUrl() : "",
						"name", product.getName() != null ? product.getName() : "",
						"price", price,
						"quantity", 1));
			});
		}

		for (Map<String, Object> item : items) {
			subtotal += ((Number) item.get("price")).longValue();
		}

		model.addAttribute("items", items);
		model.addAttribute("subtotal", subtotal);
		model.addAttribute("shippingFee", 0);
		model.addAttribute("total", subtotal);

		@SuppressWarnings("unchecked")
		Map<String, String> sessionAddress = (Map<String, String>) session.getAttribute(ORDER_ADDRESS);
		var userId = SecurityUtil.getCurrentUserId();
		Map<String, String> address = sessionAddress != null
				? sessionAddress
				: userService.getDefaultShippingAddress(userId);
		model.addAttribute("address", address);

		return "order/cart";
	}

	@PostMapping("/cart/remove")
	public String remove(
			@RequestParam("productId") UUID productId,
			HttpSession session,
			RedirectAttributes ra) {
		cartService.removeItem(session, productId);
		ra.addFlashAttribute("message", "カートから削除しました。");
		return "redirect:/cart";
	}

	@PostMapping("/cart/checkout")
	public String checkout(HttpSession session, RedirectAttributes ra) {
		var userId = SecurityUtil.getCurrentUserId();
		try {
			List<UUID> orderIds = cartService.checkout(session, userId);
			UUID firstOrderId = orderIds.get(0);
			ra.addFlashAttribute("message", orderIds.size() > 1
					? orderIds.size() + "件の注文を作成しました。"
					: "注文を作成しました。");
			return "redirect:/user/orders/" + firstOrderId;
		} catch (com.example.flea_market_app.common.exception.ValidationBusinessException e) {
			ra.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/cart";
		}
	}
}
