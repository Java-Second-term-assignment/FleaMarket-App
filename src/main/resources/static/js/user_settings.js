document.addEventListener("DOMContentLoaded", () => {
	// 汎用的な要素取得ヘルパー
	const getEl = (id) => document.getElementById(id);

	// ===== 1. タブ切り替え =====
	document.querySelectorAll(".tab").forEach(tab => {
		tab.addEventListener("click", () => {
			const tabName = tab.dataset.tab;
			if (!tabName) return;

			document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
			document.querySelectorAll(".profile-tab-content").forEach(c => c.style.display = "none");

			tab.classList.add("active");
			const targetContent = getEl("tab-" + tabName);
			if (targetContent) targetContent.style.display = "block";
		});
	});

	// ===== 2. モーダル制御 =====
	const modal = getEl("modalProfileEdit");
	const backdrop = getEl("modalProfileBackdrop");
	const openBtn = getEl("openProfileEditModalBtn");

	const closeModal = () => {
		if (modal) modal.style.display = "none";
		if (backdrop) backdrop.style.display = "none";
	};

	if (openBtn) {
		openBtn.onclick = () => {
			modal.style.display = "block";
			backdrop.style.display = "block";
		};
	}

	["modalProfileCloseBtn", "modalProfileCancelBtn"].forEach(id => {
		const btn = getEl(id);
		if (btn) btn.onclick = closeModal;
	});

	if (backdrop) backdrop.onclick = closeModal;

	// ===== 3. 画像プレビュー & ラベル連携 =====
	const fileInput = getEl("modalProfileIconFile");
	const previewImg = getEl("modalProfileIconPreview");
	const noImageSpan = getEl("modalProfileIconNoImage");
	const uploadLabel = document.querySelector('.modal-profile-uploadlabel');

	if (uploadLabel && fileInput) {
		uploadLabel.onclick = () => fileInput.click();
	}

	if (fileInput) {
		fileInput.addEventListener("change", (e) => {
			const file = e.target.files[0];
			if (file) {
				const reader = new FileReader();
				reader.onload = (event) => {
					if (previewImg) {
						previewImg.src = event.target.result;
						previewImg.style.display = "block";
					}
					if (noImageSpan) noImageSpan.style.display = "none";
				};
				reader.readAsDataURL(file);
			}
		});
	}
});