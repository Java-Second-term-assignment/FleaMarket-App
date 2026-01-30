const modal = document.getElementById("replyModal");
const backdrop = document.getElementById("replyModalBackdrop");
const closeBtn = document.getElementById("replyModalCloseBtn");
const title = document.getElementById("replyModalTitle");
const parentInput = document.getElementById("replyParentId");

document.querySelectorAll(".reply-link").forEach(link => {
	link.addEventListener("click", e => {
		e.preventDefault();
		title.textContent = link.textContent;
		parentInput.value = link.dataset.id;
		modal.classList.add("active");
		backdrop.classList.add("active");
	});
});

function closeModal() {
	modal.classList.remove("active");
	backdrop.classList.remove("active");
}

closeBtn.onclick = closeModal;
backdrop.onclick = closeModal;
