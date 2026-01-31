package com.example.flea_market_app.auth.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {

	@NotBlank(message = "メールアドレスは必須です")
	@Email(message = "メールアドレスの形式が不正です")
	private String email;

	@NotBlank(message = "パスワードは必須です")
	@Size(min = 10, max = 64, message = "パスワードは10文字以上64文字以内で、英数字・記号を含めてください")
	private String password;

	@NotBlank(message = "パスワード確認は必須です")
	private String confirmPassword;

	@AssertTrue(message = "パスワードとパスワード確認が一致しません")
	public boolean isPasswordMatch() {
		return password == null || confirmPassword == null || password.equals(confirmPassword);
	}
}
