document.addEventListener("DOMContentLoaded", () => {

	// 商品選択モーダル
	const btnSelectProduct = document.getElementById('btnSelectProduct');
	const productSelectModal = document.getElementById('productSelectModal');
	const modalCloseBtn = document.getElementById('modalCloseBtn');

	if (!btnSelectProduct) return;

	btnSelectProduct.addEventListener('click', () => {
		productSelectModal.style.display = 'flex';
	});

	modalCloseBtn.addEventListener('click', () => {
		productSelectModal.style.display = 'none';
	});

	productSelectModal
		.querySelector('.modal-backdrop')
		.addEventListener('click', () => {
			productSelectModal.style.display = 'none';
		});
});
