document.addEventListener("DOMContentLoaded", () => {

	const modal = document.getElementById("blacklistModal");
	const backdrop = document.getElementById("blacklistModalBackdrop");
	const closeBtn = document.getElementById("blacklistModalClose");
	const userIdInput = document.getElementById("blacklistUserId");
	const userNameSpan = document.getElementById("blacklistUserName");

	if (!modal || !backdrop) return;

	// ===== モーダルを開く =====
	document.querySelectorAll(".blacklist-open").forEach(btn => {
		btn.addEventListener("click", e => {
			e.preventDefault();

			userIdInput.value = btn.dataset.userId;
			userNameSpan.textContent = btn.dataset.userName;

			modal.classList.add("active");
			backdrop.classList.add("active");
		});
	});

	// ===== モーダルを閉じる =====
	function closeModal() {
		modal.classList.remove("active");
		backdrop.classList.remove("active");
	}

	closeBtn?.addEventListener("click", closeModal);
	backdrop.addEventListener("click", closeModal);

});
