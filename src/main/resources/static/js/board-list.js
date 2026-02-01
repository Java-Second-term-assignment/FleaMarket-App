document.addEventListener("DOMContentLoaded", () => {
	const modal = document.getElementById("productSelectModal");
	const btnSelectProduct = document.getElementById("btnSelectProduct");
	const modalCloseBtn = document.getElementById("modalCloseBtn");
	const backdrop = document.querySelector(".modal-backdrop");

	// モーダルを開く
	if (btnSelectProduct) {
		btnSelectProduct.addEventListener("click", () => {
			modal.style.display = "flex";
		});
	}

	// モーダルを閉じる
	const closeModal = () => {
		modal.style.display = "none";
	};

	if (modalCloseBtn) modalCloseBtn.onclick = closeModal;
	if (backdrop) backdrop.onclick = closeModal;

	// 商品を選択した時の処理
	window.selectProduct = (element) => {
		const productId = element.getAttribute("data-id");
		const productName = element.getAttribute("data-name");

		// 投稿フォーム内の隠しフィールドにIDをセット
		const hiddenInput = document.getElementById("selectedProductId");
		if (hiddenInput) {
			hiddenInput.value = productId;
		}

		// 画面上の「商品選択」ボタンの横などに選択中の名前を表示させると親切です
		const nameDisplay = document.getElementById("selectedProductName");
		if (nameDisplay) {
			nameDisplay.textContent = "選択中: " + productName;
		}

		closeModal();
	};
});