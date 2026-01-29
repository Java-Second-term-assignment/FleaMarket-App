document.addEventListener("DOMContentLoaded", () => {
	const form = document.getElementById("productForm");

	form.addEventListener("submit", (e) => {
		e.preventDefault(); // ← 直接送信しない

		const data = {
			name: document.getElementById("name").value,
			categoryText: document.getElementById("category")
				.selectedOptions[0]?.text,
			description: document.getElementById("description").value,
			price: document.getElementById("price").value
		};

		sessionStorage.setItem(
			"productConfirm",
			JSON.stringify(data)
		);

		// 確認画面へ
		window.location.href = "/item/confirm";
	});
});
