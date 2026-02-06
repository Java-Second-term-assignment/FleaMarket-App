/**
 * 掲示板詳細ページ: 投稿フォームの文字数カウント
 */
document.addEventListener("DOMContentLoaded", () => {
	const contentTextarea = document.getElementById("content");
	const charCountSpan = document.getElementById("contentCharCount");

	if (contentTextarea && charCountSpan) {
		const updateCount = () => {
			charCountSpan.textContent = contentTextarea.value.length;
		};
		contentTextarea.addEventListener("input", updateCount);
		contentTextarea.addEventListener("change", updateCount);
		updateCount();
	}
});
