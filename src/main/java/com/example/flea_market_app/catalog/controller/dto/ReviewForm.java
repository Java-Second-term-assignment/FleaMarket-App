package com.example.flea_market_app.catalog.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewForm {

	@Size(max = 500, message = "コメントは500文字以内で入力してください")
	private String comment = "";

	@NotBlank(message = "評価を選択してください")
	@Pattern(regexp = "GOOD|BAD", message = "評価は「良い」または「悪い」を選択してください")
	private String rating;
}
