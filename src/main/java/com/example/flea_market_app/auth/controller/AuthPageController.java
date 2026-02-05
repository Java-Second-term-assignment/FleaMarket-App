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
import com.example.flea_market_app.auth.service.RegistrationService;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

	private static final Logger log = LoggerFactory.getLogger(AuthPageController.class);

	private final RegistrationService registrationService;

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
	public String passwordResetRequest() {
		return "redirect:/password/forgot";
	}

	@GetMapping("/password/change")
	public String passwordChangePage() {
		return "auth/password_change";
	}

	@PostMapping("/password-change")
	public String passwordChange() {
		return "redirect:/password/change";
	}

	@GetMapping("/terms")
	public String termsPage() {
		return "auth/terms";
	}

}
