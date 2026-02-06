/**
 * 商品一覧ページ専用スクリプト。
 * 検索フォームのクリアボタンなど、一覧画面の UX のみを扱う。
 */
document.addEventListener("DOMContentLoaded", () => {
	const form = document.querySelector(".search-form");
	const clearBtn = document.querySelector(".clear-btn");
	const keywordInput = form?.querySelector('input[name="keyword"]');

	if (!form || !clearBtn || !keywordInput) {
		return;
	}

	// 検索クリア: キーワードを空にして現在の sort を維持したまま GET 送信
	clearBtn.addEventListener("click", () => {
		keywordInput.value = "";
		form.submit();
	});
});
