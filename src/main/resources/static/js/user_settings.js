document.addEventListener("DOMContentLoaded", () => {

	// ===== タブ切り替え =====
	document.querySelectorAll(".tab").forEach(tab => {
		tab.addEventListener("click", () => {
			document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
			document.querySelectorAll(".profile-tab-content")
				.forEach(c => c.style.display = "none");

			tab.classList.add("active");
			document.getElementById("tab-" + tab.dataset.tab).style.display = "block";
		});
	});

	// ===== モーダル =====
	const modal = document.getElementById("modalProfileEdit");
	const backdrop = document.getElementById("modalProfileBackdrop");

	document.getElementById("openProfileEditModalBtn")
		.onclick = () => {
			modal.style.display = "block";
			backdrop.style.display = "block";
		};

	["modalProfileCloseBtn", "modalProfileCancelBtn"].forEach(id => {
		document.getElementById(id).onclick = closeModal;
	});

	backdrop.onclick = closeModal;

	function closeModal() {
		modal.style.display = "none";
		backdrop.style.display = "none";
	}

});
