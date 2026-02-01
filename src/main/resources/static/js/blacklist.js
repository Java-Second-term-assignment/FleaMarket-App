document.addEventListener("DOMContentLoaded", () => {
	const modal = document.getElementById("blacklistModal");
	const backdrop = document.getElementById("blacklistModalBackdrop");
	const closeBtn = document.getElementById("blacklistModalClose");
	const userIdInput = document.getElementById("blacklistUserId");
	const userNameSpan = document.getElementById("blacklistUserName");

	// モーダルを開く処理
	// documentにイベントを委託することで、動的要素にも対応
	document.addEventListener("click", (e) => {
		const btn = e.target.closest(".blacklist-open");
		if (btn) {
			e.preventDefault();
			// ボタンの data- 属性から値を取得
			userIdInput.value = btn.dataset.userId;
			userNameSpan.textContent = btn.dataset.userName;

			// クラスを付与して表示
			modal.style.display = "block";
			backdrop.style.display = "block";
			// わずかに遅らせてopacityアニメーションを適用
			setTimeout(() => {
				modal.classList.add("active");
				backdrop.classList.add("active");
			}, 10);
		}
	});

	// モーダルを閉じる処理
	const closeModal = () => {
		modal.classList.remove("active");
		backdrop.classList.remove("active");
		setTimeout(() => {
			modal.style.display = "none";
			backdrop.style.display = "none";
		}, 300); // CSSのtransition時間と合わせる
	};

	if (closeBtn) closeBtn.onclick = closeModal;
	if (backdrop) backdrop.onclick = closeModal;
});