document.addEventListener("DOMContentLoaded", () => {
	const data = sessionStorage.getItem("productConfirm");
	if (!data) return;

	const product = JSON.parse(data);

	document.getElementById("confirmName").textContent = product.name;
	document.getElementById("confirmCategory").textContent = product.categoryText;
	document.getElementById("confirmDescription").textContent = product.description;
	document.getElementById("confirmPrice").textContent =
		Number(product.price).toLocaleString();

	document.querySelector(".btn-back").onclick = () => history.back();

	document.querySelector(".btn-confirm").onclick = () => {
		sessionStorage.removeItem("productConfirm");
		location.href = "/items";
	};
});
