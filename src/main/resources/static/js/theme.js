/**
 * テーマ切替（ライト/ダーク）
 * 読み込み時に localStorage から復元し、toggleTheme() で切替・保存する。
 */
(function () {
	var STORAGE_KEY = 'theme';

	function applyTheme(theme) {
		var html = document.documentElement;
		if (theme === 'light') {
			html.setAttribute('data-theme', 'light');
		} else {
			html.setAttribute('data-theme', 'dark');
		}
	}

	function init() {
		var saved = localStorage.getItem(STORAGE_KEY);
		applyTheme(saved === 'light' ? 'light' : 'dark');
	}

	function toggleTheme() {
		var current = document.documentElement.getAttribute('data-theme');
		var next = current === 'light' ? 'dark' : 'light';
		applyTheme(next);
		localStorage.setItem(STORAGE_KEY, next);
	}

	init();
	window.toggleTheme = toggleTheme;
})();
