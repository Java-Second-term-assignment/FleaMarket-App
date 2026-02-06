/**
 * パスワード表示/非表示のトグル機能
 * data-password-toggle 属性を持つ要素内のパスワード入力とトグルボタンに適用
 */
(function () {
	"use strict";

	function init() {
		document.querySelectorAll("[data-password-toggle]").forEach(function (wrapper) {
			var input = wrapper.querySelector('input[type="password"], input[type="text"]');
			var btn = wrapper.querySelector(".password-toggle-btn");
			var icon = btn && btn.querySelector(".material-symbols-outlined");

			if (!input || !btn || !icon) return;

			btn.addEventListener("click", function () {
				if (input.type === "password") {
					input.type = "text";
					icon.textContent = "visibility_off";
					btn.setAttribute("aria-label", "パスワードを隠す");
					btn.setAttribute("title", "パスワードを隠す");
				} else {
					input.type = "password";
					icon.textContent = "visibility";
					btn.setAttribute("aria-label", "パスワードを表示");
					btn.setAttribute("title", "パスワードを表示");
				}
			});
		});
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", init);
	} else {
		init();
	}
})();
