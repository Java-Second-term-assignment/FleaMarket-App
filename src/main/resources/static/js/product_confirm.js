document.addEventListener("DOMContentLoaded", () => {

	/* ===============================
	   利用規約モーダル制御
	=============================== */
	const modal = document.getElementById("termsModal");
	const closeBtn = document.getElementById("closeTermsBtn");
	const agreeCheckbox = document.getElementById("agreeTerms");
	const submitBtn = document.getElementById("submitBtn");

	// 初回表示
	modal.style.display = "flex";
	document.body.classList.add("no-scroll");

	closeBtn.addEventListener("click", () => {
		modal.style.display = "none";
		document.body.classList.remove("no-scroll");
	});

	// チェックが入るまで出品不可
	agreeCheckbox.addEventListener("change", () => {
		submitBtn.disabled = !agreeCheckbox.checked;
	});


	/* ===============================
	   既存：確認データ処理
	=============================== */
	const data = sessionStorage.getItem("productConfirm");
	if (!data) return;

	const product = JSON.parse(data);

	document.getElementById("confirmName").textContent = product.name;
	document.getElementById("confirmCategory").textContent = product.categoryText;
	document.getElementById("confirmDescription").textContent = product.description;
	document.getElementById("confirmPrice").textContent =
		Number(product.price).toLocaleString();

	document.querySelector(".btn-back").onclick = () => history.back();

	// 送信後に sessionStorage を掃除
	document.querySelector("form").addEventListener("submit", () => {
		sessionStorage.removeItem("productConfirm");
	});

});
