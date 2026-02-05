/**
 * 商品詳細：未ログイン時に「購入に進む」「カートに入れる」を押した場合、
 * 商品一覧へ飛ばさず「ログインはお済みですか？」モーダルを表示してログインへ誘導する。
 */
(function () {
	"use strict";

	var body = document.body;
	var isAnonymous = body && body.getAttribute("data-anonymous") === "true";
	if (!isAnonymous) return;

	var main = document.querySelector(".main-container");
	var productId = main && main.getAttribute("data-product-id");
	if (!productId) return;

	var modal = document.getElementById("loginRequiredModal");
	var modalLink = document.getElementById("loginRequiredModalLink");
	var modalCloseBtn = document.getElementById("loginRequiredModalClose");
	if (!modal || !modalLink) return;

	function setReturnUrlAndShow(url) {
		modalLink.href = "/login?returnUrl=" + encodeURIComponent(url);
		modal.style.display = "flex";
		modal.setAttribute("aria-hidden", "false");
	}

	function closeModal() {
		modal.style.display = "none";
		modal.setAttribute("aria-hidden", "true");
	}

	// 購入に進む
	var btnPurchase = document.querySelector(".btn-purchase");
	if (btnPurchase && !btnPurchase.classList.contains("disabled")) {
		btnPurchase.addEventListener("click", function (e) {
			e.preventDefault();
			setReturnUrlAndShow("/order/confirm?productId=" + productId);
		});
	}

	// カートに入れる
	var cartForm = document.querySelector("form[action*='/cart/add']");
	if (cartForm) {
		cartForm.addEventListener("submit", function (e) {
			e.preventDefault();
			setReturnUrlAndShow("/products/" + productId);
		});
	}

	// モーダルを閉じる
	if (modalCloseBtn) modalCloseBtn.addEventListener("click", closeModal);
	modal.addEventListener("click", function (e) {
		if (e.target === modal) closeModal();
	});
	document.addEventListener("keydown", function (e) {
		if (e.key === "Escape" && modal.style.display === "flex") closeModal();
	});
})();

/**
 * お気に入りトグル：ログイン時は API で追加/解除、未ログイン時はログインモーダルを表示。
 */
(function () {
	"use strict";

	document.addEventListener("DOMContentLoaded", function () {
		var main = document.querySelector(".main-container");
		if (!main) return;
		var productId = main.getAttribute("data-product-id");
		if (!productId) return;

		var body = document.body;
		var isAnonymous = body && body.getAttribute("data-anonymous") === "true";
		var toggleButtons = document.querySelectorAll(".favorite-toggle-btn");
		var errorEl = document.getElementById("favoriteError");
		var iconSpan = document.querySelector(".favorite-toggle-btn .favorite-icon");
		var textBtn = document.querySelector(".favorite-toggle-btn.btn-gray");

		function getIsFavorited() {
			return main.getAttribute("data-is-favorited") === "true";
		}

		function setFavorited(favorited) {
			main.setAttribute("data-is-favorited", favorited ? "true" : "false");
			if (iconSpan) iconSpan.textContent = favorited ? "favorite" : "favorite_border";
			toggleButtons.forEach(function (btn) {
				if (favorited) btn.classList.add("is-favorited"); else btn.classList.remove("is-favorited");
				btn.setAttribute("aria-pressed", favorited ? "true" : "false");
			});
			if (textBtn) textBtn.textContent = favorited ? "お気に入り解除" : "お気に入り";
		}

		function showFavoriteError(msg) {
			if (errorEl) {
				errorEl.textContent = msg || "";
				errorEl.classList.toggle("visible", !!msg);
			}
		}

		function showLoginModal() {
			var modal = document.getElementById("loginRequiredModal");
			var modalLink = document.getElementById("loginRequiredModalLink");
			if (!modal || !modalLink) return;
			modalLink.href = "/login?returnUrl=" + encodeURIComponent("/products/" + productId);
			modal.style.display = "flex";
			modal.setAttribute("aria-hidden", "false");
		}

		function handleFavoriteClick(e) {
			e.preventDefault();
			if (isAnonymous) {
				showLoginModal();
				return;
			}
			showFavoriteError("");
			var favorited = getIsFavorited();
			var url = "/user/favorites/" + productId;
			var method = favorited ? "DELETE" : "POST";
			var opts = { method: method, credentials: "same-origin", headers: {} };
			if (method === "POST") {
				opts.headers["Content-Type"] = "application/json";
				opts.body = JSON.stringify({ itemId: productId });
			}
			fetch(method === "POST" ? "/user/favorites" : url, opts)
				.then(function (res) {
					if (res.status === 204) {
						setFavorited(!favorited);
						return;
					}
					return res.json().then(function (data) {
						var msg = (data && data.message) ? data.message : "操作に失敗しました。";
						throw new Error(msg);
					}).catch(function () { throw new Error("操作に失敗しました。"); });
				})
				.catch(function (err) {
					showFavoriteError(err.message || "操作に失敗しました。");
				});
		}

		toggleButtons.forEach(function (btn) {
			btn.addEventListener("click", handleFavoriteClick);
		});
	});
})();
