document.addEventListener("DOMContentLoaded", () => {
	const cancelBtn = document.querySelector(".btn-cancel-open");
	const cancelModal = document.getElementById("cancelModal");
	const cancelBackdrop = document.getElementById("cancelModalBackdrop");
	const cancelForm = document.getElementById("cancelForm");
	const cancelModalClose = document.getElementById("cancelModalClose");
	const cancelModalCancel = document.getElementById("cancelModalCancel");

	const reviewBtn = document.querySelector(".btn-review-open");
	const reviewModal = document.getElementById("reviewModal");
	const reviewBackdrop = document.getElementById("reviewModalBackdrop");
	const reviewModalClose = document.getElementById("reviewModalClose");
	const reviewModalCancel = document.getElementById("reviewModalCancel");

	function openCancelModal() {
		if (cancelModal) cancelModal.classList.add("active");
		if (cancelBackdrop) cancelBackdrop.classList.add("active");
	}

	function closeCancelModal() {
		if (cancelModal) cancelModal.classList.remove("active");
		if (cancelBackdrop) cancelBackdrop.classList.remove("active");
	}

	function openReviewModal() {
		if (reviewModal) reviewModal.classList.add("active");
		if (reviewBackdrop) reviewBackdrop.classList.add("active");
	}

	function closeReviewModal() {
		if (reviewModal) reviewModal.classList.remove("active");
		if (reviewBackdrop) reviewBackdrop.classList.remove("active");
	}

	if (cancelBtn) {
		cancelBtn.addEventListener("click", () => {
			const orderId = cancelBtn.getAttribute("data-order-id");
			if (orderId && cancelForm) {
				cancelForm.action = "/user/orders/" + orderId + "/cancel";
			}
			openCancelModal();
		});
	}

	if (cancelBackdrop) cancelBackdrop.addEventListener("click", closeCancelModal);
	if (cancelModalClose) cancelModalClose.addEventListener("click", closeCancelModal);
	if (cancelModalCancel) cancelModalCancel.addEventListener("click", closeCancelModal);

	if (reviewBtn) {
		reviewBtn.addEventListener("click", () => {
			const orderId = reviewBtn.getAttribute("data-order-id");
			const reviewForm = document.getElementById("reviewForm");
			if (orderId && reviewForm) {
				reviewForm.action = "/user/orders/" + orderId + "/review";
			}
			openReviewModal();
		});
	}

	if (reviewBackdrop) reviewBackdrop.addEventListener("click", closeReviewModal);
	if (reviewModalClose) reviewModalClose.addEventListener("click", closeReviewModal);
	if (reviewModalCancel) reviewModalCancel.addEventListener("click", closeReviewModal);
});
