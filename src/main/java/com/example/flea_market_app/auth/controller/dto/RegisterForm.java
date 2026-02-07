package com.example.flea_market_app.auth.controller.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {

	@NotBlank(message = "表示名は必須です")
	@Size(max = 50, message = "表示名は50文字以内で入力してください")
	private String displayName;

	/** 新規登録では未入力可（設定画面などで後から登録） */
	@Size(max = 100)
	private String recipientName;

	@Size(max = 100)
	private String recipientNameFurigana;

	@Size(max = 20)
	private String postalCode;

	@Size(max = 500)
	private String address;

	@Size(max = 30)
	private String phone;

	@NotNull(message = "生年月日は必須です")
	@Past(message = "生年月日は過去の日付を入力してください")
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate dateOfBirth;

	/** 性別（任意）。MALE / FEMALE / OTHER または空 */
	@Size(max = 20, message = "性別は20文字以内で入力してください")
	@Pattern(regexp = "^(|MALE|FEMALE|OTHER)$", message = "性別は選択肢から選んでください")
	private String gender;

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
		return password != null && confirmPassword != null && password.equals(confirmPassword);
	}
}
