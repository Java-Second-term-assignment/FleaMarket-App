package com.example.flea_market_app.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.flea_market_app.admin.service.AdminDashboardQueryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

	private static final Logger log = LoggerFactory.getLogger(AdminPageController.class);

	private final AdminDashboardQueryService adminDashboardQueryService;

	/** 管理画面のトップURL: ダッシュボードへリダイレクト */
	@GetMapping("/admin")
	public String adminTop() {
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/admin/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("stats", adminDashboardQueryService.getDashboardStats());
		log.info("Admin dashboard displayed");
		return "admin/dashboard";
	}

	@GetMapping("/admin/users")
	public String users(Model model) {
		model.addAttribute("users", adminDashboardQueryService.getUsersForDashboard());
		log.info("Admin users list displayed");
		return "admin/users";
	}

	@GetMapping("/admin/products")
	public String products(Model model) {
		model.addAttribute("products", adminDashboardQueryService.getProductsForDashboard());
		log.info("Admin products list displayed");
		return "admin/products";
	}

	@GetMapping("/admin/blacklist")
	public String blacklist() {
		log.info("Admin blacklist displayed");
		return "admin/blacklist";
	}
}
