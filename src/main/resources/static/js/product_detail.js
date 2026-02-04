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
