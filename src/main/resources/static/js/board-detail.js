document.addEventListener("DOMContentLoaded", () => {
	const modal = document.getElementById("productSelectModal");
	const btnSelect = document.getElementById("btnSelectProduct");
	const btnClose = document.getElementById("modalCloseBtn");
	const backdrop = document.querySelector("#productSelectModal .modal-backdrop"); // IDまたはクラスで確実に取得

	// --- モーダル制御 ---
	const openModal = () => {
		if (modal) {
			modal.style.display = "flex"; // 構造上必要な場合
			// 0.01秒遅らせてクラスを付与することでCSSアニメーションを効かせる
			setTimeout(() => modal.classList.add("active"), 10);
		}
	};

	const closeModal = () => {
		if (modal) {
			modal.classList.remove("active");
			// アニメーションが終わるのを待ってから非表示にする（CSSが0.3sなら300）
			setTimeout(() => {
				if (!modal.classList.contains("active")) {
					modal.style.display = "none";
				}
			}, 300);
		}
	};

	if (btnSelect) btnSelect.onclick = openModal;
	if (btnClose) btnClose.onclick = closeModal;
	if (backdrop) backdrop.onclick = closeModal;

	// --- カテゴリーフィルター処理 ---
	window.filterCategory = (categoryName) => {
		const searchCategoryInput = document.getElementById("searchCategory");
		const searchForm = document.getElementById("searchForm");

		if (searchCategoryInput && searchForm) {
			searchCategoryInput.value = categoryName;
			searchForm.submit();
		}
	};

	// --- 商品選択処理 ---
	window.selectProduct = (id, name) => {
		const idInput = document.getElementById("selectedProductId");
		const nameDisplay = document.getElementById("selectedProductName");

		if (idInput) idInput.value = id;
		if (nameDisplay) {
			nameDisplay.textContent = name;
			nameDisplay.style.color = "#F2EEB8"; // 選択されたことがわかるように色を変更
		}

		closeModal();
	};

	// Escキーでモーダルを閉じる
	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape') closeModal();
	});
});