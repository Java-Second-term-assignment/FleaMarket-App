document.addEventListener("DOMContentLoaded", () => {

	// ===== タブ切り替え =====
	const tabs = document.querySelectorAll(".admin-tab");
	const contents = document.querySelectorAll(".admin-tab-content");

	if (tabs.length > 0) {
		tabs.forEach(tab => {
			tab.addEventListener("click", () => {
				const target = tab.dataset.target;

				// タブの active 切り替え
				tabs.forEach(t => t.classList.remove("active"));
				tab.classList.add("active");

				// コンテンツの表示切り替え
				contents.forEach(c => {
					c.classList.toggle("active", c.id === target);
				});
			});
		});
	}

	// ===== 行クリック（詳細遷移など） =====
	document.querySelectorAll("[data-href]").forEach(row => {
		row.addEventListener("click", () => {
			window.location.href = row.dataset.href;
		});
	});

});
