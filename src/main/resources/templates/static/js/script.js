// textareaの文字数カウント
document.addEventListener('DOMContentLoaded', function() {
	var textarea = document.getElementById('review-text');
	var counter = document.getElementById('review-charcount');
	if (textarea && counter) {
		textarea.addEventListener('input', function() {
			counter.textContent = textarea.value.length + '/500';
		});
	}

	// 星の色の制御（selectに応じて）
	var star = document.getElementById('single-star');
	var ratingInput = document.getElementById('review-rating');
	if (star && ratingInput) {
		ratingInput.addEventListener('change', function() {
			if (ratingInput.value && ratingInput.value > 0) {
				star.style.color = "#f5c518";
			} else {
				star.style.color = "#c8cbe0";
			}
		});
		// フォーカスなど外したときも薄色に戻す
		ratingInput.addEventListener('blur', function() {
			if (!ratingInput.value) star.style.color = "#c8cbe0";
		});
		// 星をクリックでもセレクトをフォーカス
		star.addEventListener('click', function() {
			ratingInput.focus();
		});
	}
});

// フォーム送信処理
function handleReviewFormSubmit(event) {
	event.preventDefault();
	var textarea = document.getElementById('review-text');
	var rating = document.getElementById('review-rating');
	if (!textarea.value.trim()) {
		alert('レビュー内容を入力してください。');
		textarea.focus();
		return false;
	}
	if (!rating.value || rating.value < 0.5 || rating.value > 5) {
		alert('星評価を0.5〜5.0で選択してください。');
		return false;
	}
	alert("レビューを送信しました。ありがとうございます！");
	textarea.value = "";
	rating.value = "";
	document.getElementById('review-charcount').textContent = "0/500";
	var star = document.getElementById('single-star');
	if (star) star.style.color = '#c8cbe0';
	return false;
}


// 画像切り替え用のシンプルなスクリプト
function changeImage(src, element) {
	// メイン画像を書き換え
	document.getElementById('target-main-image').src = src;

	// 全てのサムネイルからactiveクラスを消す
	const thumbs = document.querySelectorAll('.thumbnail-item');
	thumbs.forEach(thumb => thumb.classList.remove('active'));

	// クリックした要素にactiveクラスをつける
	element.classList.add('active');
}