document.addEventListener("DOMContentLoaded", () => {
	const modal = document.getElementById("adminEditModal");
	const backdrop = document.getElementById("adminModalBackdrop");
	const closeBtns = document.querySelectorAll(".close-modal");
	const editForm = document.getElementById("editForm");

	// --- タブ切り替え ---
	document.querySelectorAll(".tab-btn").forEach(btn => {
		btn.addEventListener("click", () => {
			const targetId = btn.getAttribute("data-target");
			if (!targetId) return;

			document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
			document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));

			btn.classList.add("active");
			document.getElementById(targetId).classList.add("active");
		});
	});

	// --- モーダル制御 ---
	const openModal = (title, actionUrl, isUser = true) => {
		document.getElementById("modalTitle").textContent = title;
		editForm.action = actionUrl;

		// 入力項目の表示・非表示切り替え
		document.getElementById("emailGroup").style.display = isUser ? "flex" : "none";
		document.getElementById("priceGroup").style.display = isUser ? "none" : "flex";

		modal.style.display = "flex";
		backdrop.style.display = "block";
		setTimeout(() => {
			modal.classList.add("active");
			backdrop.classList.add("active");
		}, 10);
	};

	const closeModal = () => {
		modal.classList.remove("active");
		backdrop.classList.remove("active");
		setTimeout(() => {
			modal.style.display = "none";
			backdrop.style.display = "none";
		}, 300);
	};

	// ユーザー編集ボタン
	document.querySelectorAll(".edit-user-btn").forEach(btn => {
		btn.onclick = () => {
			document.getElementById("editId").value = btn.dataset.id;
			document.getElementById("editName").value = btn.dataset.name;
			document.getElementById("editEmail").value = btn.dataset.email;
			openModal("ユーザー編集", "/admin/users/update", true);
		};
	});

	// 商品編集ボタン
	document.querySelectorAll(".edit-product-btn").forEach(btn => {
		btn.onclick = () => {
			document.getElementById("editId").value = btn.dataset.id;
			document.getElementById("editName").value = btn.dataset.name;
			document.getElementById("editPrice").value = btn.dataset.price;
			openModal("商品編集", "/admin/products/update", false);
		};
	});

	closeBtns.forEach(btn => btn.onclick = closeModal);
	backdrop.onclick = closeModal;

	// --- 永久削除確認モーダル ---
	const deleteModal = document.getElementById("deleteConfirmModal");
	const deleteBackdrop = document.getElementById("deleteConfirmModalBackdrop");
	const deleteForm = document.getElementById("deleteConfirmForm");
	const deleteTargetId = document.getElementById("deleteConfirmTargetId");
	const deleteTargetName = document.getElementById("deleteConfirmTargetName");
	const deleteModalClose = document.getElementById("deleteConfirmModalClose");

	document.addEventListener("click", (e) => {
		const btn = e.target.closest(".delete-confirm-open");
		if (btn) {
			e.preventDefault();
			deleteForm.action = btn.dataset.formAction || "";
			deleteTargetId.value = btn.dataset.targetId || "";
			deleteTargetName.textContent = btn.dataset.targetName || "対象";
			deleteModal.style.display = "block";
			deleteBackdrop.style.display = "block";
			setTimeout(() => {
				deleteModal.classList.add("active");
				deleteBackdrop.classList.add("active");
			}, 10);
		}
	});

	const closeDeleteModal = () => {
		deleteModal.classList.remove("active");
		deleteBackdrop.classList.remove("active");
		setTimeout(() => {
			deleteModal.style.display = "none";
			deleteBackdrop.style.display = "none";
		}, 300);
	};

	if (deleteModalClose) deleteModalClose.onclick = closeDeleteModal;
	if (deleteBackdrop) deleteBackdrop.onclick = closeDeleteModal;
});