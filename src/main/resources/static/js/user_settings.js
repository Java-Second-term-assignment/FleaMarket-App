document.addEventListener("DOMContentLoaded", () => {
	// 汎用的な要素取得ヘルパー
	const getEl = (id) => document.getElementById(id);

	// ===== 1. タブ切り替え =====
	document.querySelectorAll(".tab").forEach(tab => {
		tab.addEventListener("click", () => {
			const tabName = tab.dataset.tab;
			if (!tabName) return;

			document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
			document.querySelectorAll(".profile-tab-content").forEach(c => c.style.display = "none");

			tab.classList.add("active");
			const targetContent = getEl("tab-" + tabName);
			if (targetContent) targetContent.style.display = "block";
		});
	});

	// ===== 2. モーダル制御 =====
	const modal = getEl("modalProfileEdit");
	const backdrop = getEl("modalProfileBackdrop");
	const openBtn = getEl("openProfileEditModalBtn");

	const closeModal = () => {
		if (modal) modal.style.display = "none";
		if (backdrop) backdrop.style.display = "none";
	};

	if (openBtn) {
		openBtn.onclick = () => {
			modal.style.display = "block";
			backdrop.style.display = "block";
		};
	}

	["modalProfileCloseBtn", "modalProfileCancelBtn"].forEach(id => {
		const btn = getEl(id);
		if (btn) btn.onclick = closeModal;
	});

	if (backdrop) backdrop.onclick = closeModal;

	// ===== 3. 画像プレビュー & ラベル連携 =====
	const fileInput = getEl("modalProfileIconFile");
	const previewImg = getEl("modalProfileIconPreview");
	const noImageSpan = getEl("modalProfileIconNoImage");
	const uploadLabel = document.querySelector('.modal-profile-uploadlabel');

	if (uploadLabel && fileInput) {
		uploadLabel.onclick = () => fileInput.click();
	}

	if (fileInput) {
		fileInput.addEventListener("change", (e) => {
			const file = e.target.files[0];
			if (file) {
				const reader = new FileReader();
				reader.onload = (event) => {
					if (previewImg) {
						previewImg.src = event.target.result;
						previewImg.style.display = "block";
					}
					if (noImageSpan) noImageSpan.style.display = "none";
				};
				reader.readAsDataURL(file);
			}
		});
	}

	// ===== 4. プロフィール保存（PATCH /api/user/me + 画像ありなら PUT /api/user/me/profile-image） =====
	const profileEditForm = document.getElementById("profileEditForm");
	if (profileEditForm) {
		profileEditForm.addEventListener("submit", async (e) => {
			e.preventDefault();

			const displayNameEl = getEl("modalProfileEditTitle");
			const captionEl = getEl("modalProfileEditCaption");
			const displayName = displayNameEl ? displayNameEl.value.trim() : "";
			const caption = captionEl ? captionEl.value.trim() : "";
			const imageFile = fileInput && fileInput.files && fileInput.files[0] ? fileInput.files[0] : null;

			try {
				// 表示名・自己紹介を更新
				const patchRes = await fetch("/api/user/me", {
					method: "PATCH",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ displayName, caption }),
					credentials: "same-origin"
				});
				if (!patchRes.ok) {
					const text = await patchRes.text();
					alert("プロフィールの保存に失敗しました。\n" + (text || patchRes.status));
					return;
				}

				let newImageUrl = null;
				if (imageFile) {
					const formData = new FormData();
					formData.append("image", imageFile);
					const putRes = await fetch("/api/user/me/profile-image", {
						method: "PUT",
						body: formData,
						credentials: "same-origin"
					});
					if (!putRes.ok) {
						const text = await putRes.text();
						alert("プロフィール画像の更新に失敗しました。\n" + (text || putRes.status));
						return;
					}
					const json = await putRes.json();
					newImageUrl = json.imageUrl || null;
				}

				// 画面上の表示を更新
				const profileDisplayTitle = getEl("profileDisplayTitle");
				const profileDisplayCaption = getEl("profileDisplayCaption");
				const profileIconDisplay = getEl("profileIconDisplay");
				if (profileDisplayTitle) profileDisplayTitle.textContent = displayName || "表示名";
				if (profileDisplayCaption) profileDisplayCaption.textContent = caption || "プロフィールはまだ未記入です。";
				if (newImageUrl && profileIconDisplay) {
					let img = profileIconDisplay.querySelector("img");
					const span = profileIconDisplay.querySelector("span");
					if (!img) {
						img = document.createElement("img");
						img.alt = "icon";
						profileIconDisplay.appendChild(img);
					}
					img.src = newImageUrl;
					img.style.display = "block";
					if (span) span.style.display = "none";
					profileIconDisplay.dataset.imgsrc = newImageUrl;
				}

				closeModal();
				if (fileInput) fileInput.value = "";
				if (previewImg) { previewImg.style.display = "none"; previewImg.src = ""; }
				if (noImageSpan) noImageSpan.style.display = "inline-flex";
			} catch (err) {
				console.error(err);
				alert("プロフィールの保存中にエラーが発生しました。");
			}
		});
	}
});