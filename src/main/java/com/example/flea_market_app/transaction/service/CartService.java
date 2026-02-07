package com.example.flea_market_app.transaction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * セッションベースのカート管理。
 * セッションキー "cart" に List&lt;UUID&gt;（商品ID）を保持する。
 */
@Service
@RequiredArgsConstructor
public class CartService {

	private static final String SESSION_KEY_CART = "cart";
	private static final String ORDER_ADDRESS = "orderAddress";

	private static final Map<String, String> DEFAULT_ADDRESS = Map.of("name", "", "postcode", "", "fullAddress", "");

	private final OrderService orderService;
	private final UserService userService;

	@SuppressWarnings("unchecked")
	public List<UUID> getItemIds(HttpSession session) {
		List<UUID> cart = (List<UUID>) session.getAttribute(SESSION_KEY_CART);
		return cart != null ? new ArrayList<>(cart) : new ArrayList<>();
	}

	public void addItem(HttpSession session, UUID productId) {
		List<UUID> cart = getItemIds(session);
		cart.add(productId);
		session.setAttribute(SESSION_KEY_CART, cart);
	}

	public void removeItem(HttpSession session, UUID productId) {
		List<UUID> cart = getItemIds(session);
		cart.remove(productId);
		session.setAttribute(SESSION_KEY_CART, cart);
	}

	public void clear(HttpSession session) {
		session.removeAttribute(SESSION_KEY_CART);
	}

	/**
	 * カート内の商品を注文に変換する。商品ごとに1注文を作成する。
	 *
	 * @param session HTTPセッション（カート・配送先）
	 * @param userId  購入者ID
	 * @return 作成した注文IDのリスト（最初の注文でリダイレクトする想定）
	 */
	public List<UUID> checkout(HttpSession session, UUID userId) {
		List<UUID> itemIds = getItemIds(session);
		if (itemIds.isEmpty()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"カートが空です。");
		}

		@SuppressWarnings("unchecked")
		Map<String, String> sessionAddress = (Map<String, String>) session.getAttribute(ORDER_ADDRESS);
		Map<String, String> addressMap = sessionAddress != null ? sessionAddress : userService.getDefaultShippingAddress(userId);
		String addressSnapshot = toJsonSnapshot(addressMap);

		List<UUID> createdOrderIds = new ArrayList<>();
		for (UUID itemId : itemIds) {
			try {
				UUID orderId = orderService.createOrderFromProduct(itemId, userId, addressSnapshot);
				createdOrderIds.add(orderId);
			} catch (ValidationBusinessException e) {
				// 自分の出品などでスキップ
				continue;
			}
		}

		clear(session);

		if (createdOrderIds.isEmpty()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"注文を作成できませんでした。自分の出品商品や売り切れの商品がある場合はスキップされます。");
		}

		return createdOrderIds;
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
