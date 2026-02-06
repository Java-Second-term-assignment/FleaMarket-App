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

	// --- 取引チャット ---
	const chatEl = document.getElementById("orderDetailChat");
	const chatMessages = document.getElementById("chatMessages");
	const chatInput = document.getElementById("chatInput");
	const chatSendBtn = document.getElementById("chatSendBtn");
	const chatRefreshBtn = document.getElementById("chatRefreshBtn");
	const chatError = document.getElementById("chatError");

	if (chatEl && chatMessages) {
		const orderId = chatEl.getAttribute("data-order-id");
		const currentUserId = chatEl.getAttribute("data-current-user-id");
		const counterpartyName = chatEl.getAttribute("data-counterparty-name") || "相手";

		function showChatError(msg) {
			if (chatError) {
				chatError.textContent = msg || "";
				chatError.classList.toggle("visible", !!msg);
			}
		}

		function formatMessageTime(isoString) {
			if (!isoString) return "";
			const d = new Date(isoString);
			if (isNaN(d.getTime())) return isoString;
			const y = d.getFullYear();
			const m = String(d.getMonth() + 1).padStart(2, "0");
			const day = String(d.getDate()).padStart(2, "0");
			const h = String(d.getHours()).padStart(2, "0");
			const min = String(d.getMinutes()).padStart(2, "0");
			return y + "/" + m + "/" + day + " " + h + ":" + min;
		}

		function renderMessages(list) {
			chatMessages.innerHTML = "";
			if (!list || list.length === 0) {
				chatMessages.innerHTML = "<p class=\"chat-empty\">まだメッセージはありません。</p>";
				return;
			}
			list.forEach((msg) => {
				const isOwn = msg.senderId === currentUserId;
				const label = isOwn ? "自分" : counterpartyName;
				const bubble = document.createElement("div");
				bubble.className = "chat-bubble " + (isOwn ? "chat-bubble-own" : "chat-bubble-other");
				bubble.innerHTML =
					"<span class=\"chat-bubble-label\">" + escapeHtml(label) + "</span>" +
					"<span class=\"chat-bubble-time\">" + escapeHtml(formatMessageTime(msg.createdAt)) + "</span>" +
					"<p class=\"chat-bubble-content\">" + escapeHtml(msg.content || "") + "</p>";
				chatMessages.appendChild(bubble);
			});
			chatMessages.scrollTop = chatMessages.scrollHeight;
		}

		function escapeHtml(s) {
			if (s == null) return "";
			const div = document.createElement("div");
			div.textContent = s;
			return div.innerHTML;
		}

		function loadMessages() {
			showChatError("");
			fetch("/user/orders/" + orderId + "/messages?page=0&size=50", { credentials: "same-origin" })
				.then((res) => {
					if (!res.ok) {
						if (res.status === 401) throw new Error("ログインし直してください。");
						if (res.status === 403) throw new Error("この取引のチャットを表示する権限がありません。");
						if (res.status === 404) throw new Error("注文が見つかりません。");
						throw new Error("メッセージの取得に失敗しました。");
					}
					return res.json();
				})
				.then((body) => renderMessages(body && body.data != null ? body.data : body))
				.catch((err) => showChatError(err.message || "エラーが発生しました。"));
		}

		function sendMessage() {
			const content = (chatInput && chatInput.value) ? chatInput.value.trim() : "";
			if (!content) {
				showChatError("メッセージを入力してください。");
				return;
			}
			showChatError("");
			if (chatSendBtn) chatSendBtn.disabled = true;
			fetch("/user/orders/" + orderId + "/messages", {
				method: "POST",
				credentials: "same-origin",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ content: content })
			})
				.then((res) => {
					if (!res.ok) {
						return res.json().then((body) => {
							const msg = (body && body.message) ? body.message : "送信に失敗しました。";
							throw new Error(msg);
						}).catch(() => { throw new Error("送信に失敗しました。"); });
					}
				})
				.then(() => {
					if (chatInput) chatInput.value = "";
					loadMessages();
				})
				.catch((err) => showChatError(err.message || "送信に失敗しました。"))
				.finally(() => { if (chatSendBtn) chatSendBtn.disabled = false; });
		}

		loadMessages();
		if (chatSendBtn) chatSendBtn.addEventListener("click", sendMessage);
		if (chatRefreshBtn) chatRefreshBtn.addEventListener("click", loadMessages);
		if (chatInput) {
			chatInput.addEventListener("keydown", (e) => {
				if (e.key === "Enter" && !e.shiftKey) {
					e.preventDefault();
					sendMessage();
				}
			});
		}
	}
});
