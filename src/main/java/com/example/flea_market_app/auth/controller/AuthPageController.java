package com.example.flea_market_app.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.auth.controller.dto.RegisterForm;
import com.example.flea_market_app.auth.service.PasswordChangeService;
import com.example.flea_market_app.auth.service.PasswordResetService;
import com.example.flea_market_app.auth.service.RegistrationService;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.common.validation.EmailValidator;
import com.example.flea_market_app.config.security.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

	private static final Logger log = LoggerFactory.getLogger(AuthPageController.class);

	private final RegistrationService registrationService;
	private final PasswordResetService passwordResetService;
	private final PasswordChangeService passwordChangeService;

	@GetMapping("/login")
	public String loginPage(@RequestParam(required = false) String returnUrl, Model model) {
		// 相対パスのみモデルに渡す（オープンリダイレクト・XSS対策）
		if (returnUrl != null && !returnUrl.isBlank()) {
			String trimmed = returnUrl.trim();
			if (trimmed.startsWith("/") && !trimmed.startsWith("//")) {
				model.addAttribute("returnUrl", trimmed);
			}
		}
		return "auth/login";
	}

	/** 管理者用ログインページ（一般ユーザーとは別エントリーポイント） */
	@GetMapping("/admin/login")
	public String adminLoginPage() {
		return "auth/admin_login";
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("userForm", new RegisterForm());
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(
			@Valid RegisterForm form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("userForm", form);
			return "auth/register";
		}

		try {
			registrationService.register(form);
			log.info("User registered successfully: email={}", form.getEmail());
		} catch (ValidationBusinessException e) {
			log.warn("Registration validation failed: {}", e.getMessage());
			model.addAttribute("userForm", form);
			model.addAttribute("errorMessage", e.getMessage());
			return "auth/register";
		}

		redirectAttributes.addFlashAttribute("successMessage", "会員登録が完了しました。ログインしてください。");
		return "redirect:/login";
	}

	@GetMapping("/password/forgot")
	public String passwordForgotPage() {
		return "auth/password_reset_request";
	}

	@PostMapping("/password-reset-request")
	public String passwordResetRequest(
			@RequestParam(name = "email", required = false) String email,
			RedirectAttributes redirectAttributes) {
		if (email != null && !email.isBlank()) {
			try {
				EmailValidator.validate(email.trim());
			} catch (ValidationBusinessException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "メールアドレスの形式が正しくありません。");
				return "redirect:/password/forgot";
			}
			passwordResetService.requestReset(email.trim());
		}
		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスにパスワード再設定用のリンクを送信しました。有効期限は1時間です。");
		return "redirect:/password/forgot";
	}

	@GetMapping("/password/reset")
	public String passwordResetFormPage(@RequestParam(name = "token", required = false) String token,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (token == null || token.isBlank()) {
			redirectAttributes.addFlashAttribute("errorMessage", "リンクが無効です。再度パスワード再設定を申請してください。");
			return "redirect:/password/forgot";
		}
		model.addAttribute("token", token);
		return "auth/password_reset_form";
	}

	@PostMapping("/password/reset")
	public String passwordResetSubmit(
			@RequestParam(name = "token", required = false) String token,
			@RequestParam(name = "password", required = false) String password,
			@RequestParam(name = "confirmPassword", required = false) String confirmPassword,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (token == null || token.isBlank()) {
			model.addAttribute("errorMessage", "リンクが無効です。再度パスワード再設定を申請してください。");
			return "auth/password_reset_form";
		}
		if (password == null || !password.equals(confirmPassword)) {
			model.addAttribute("token", token);
			model.addAttribute("errorMessage", "パスワードとパスワード確認が一致しません。");
			return "auth/password_reset_form";
		}
		try {
			passwordResetService.resetPassword(token, password);
		} catch (ValidationBusinessException e) {
			model.addAttribute("token", token);
			model.addAttribute("errorMessage", "リンクが無効または期限切れです。再度パスワード再設定を申請してください。");
			return "auth/password_reset_form";
		}
		redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。新しいパスワードでログインしてください。");
		return "redirect:/login";
	}

	@GetMapping("/password/change")
	public String passwordChangePage() {
		return "auth/password_change";
	}

	@PostMapping("/password-change")
	public String passwordChange(
			@RequestParam(name = "password", required = false) String password,
			@RequestParam(name = "confirmPassword", required = false) String confirmPassword,
			RedirectAttributes redirectAttributes) {
		UUID userId = SecurityUtil.getCurrentUserId();
		if (password == null || password.isBlank()) {
			redirectAttributes.addFlashAttribute("errorMessage", "パスワードとパスワード確認が一致しません。");
			return "redirect:/password/change";
		}
		if (!password.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute("errorMessage", "パスワードとパスワード確認が一致しません。");
			return "redirect:/password/change";
		}
		try {
			passwordChangeService.changePassword(userId, password);
		} catch (ValidationBusinessException e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"パスワードは10文字以上64文字以内で、英数字・記号を含む必要があります。");
			return "redirect:/password/change";
		}
		redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。");
		return "redirect:/user/settings";
	}

	@GetMapping("/terms")
	public String termsPage() {
		return "auth/terms";
	}

}
