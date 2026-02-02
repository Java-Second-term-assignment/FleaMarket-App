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
		document.getElementById("emailGroup").style.display = isUser ? "block" : "none";
		document.getElementById("priceGroup").style.display = isUser ? "none" : "block";

		modal.style.display = "block";
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
});