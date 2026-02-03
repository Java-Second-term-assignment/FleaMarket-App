package com.example.flea_market_app.admin.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.admin.service.AdminDashboardQueryService;
import com.example.flea_market_app.admin.service.AdminService;
import com.example.flea_market_app.admin.service.AdminStatsService;
import com.example.flea_market_app.admin.service.AdminUserService;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

	private static final Logger log = LoggerFactory.getLogger(AdminPageController.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final AdminDashboardQueryService adminDashboardQueryService;
	private final AdminStatsService adminStatsService;
	private final AdminUserService adminUserService;
	private final AdminService adminService;

	/** 管理画面のトップURL: ダッシュボードへリダイレクト */
	@GetMapping("/admin")
	public String adminTop() {
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/admin/dashboard")
	public String dashboard(Model model) throws JsonProcessingException {
		int days = adminStatsService.getDefaultDays();
		model.addAttribute("stats", adminStatsService.getDashboardStats());
		model.addAttribute("itemStatusCounts", adminStatsService.getItemStatusCounts());
		model.addAttribute("itemCountsByDay", adminStatsService.getItemCountsByDay(days));
		model.addAttribute("userSignupCountsByDay", adminStatsService.getUserSignupCountsByDay(days));
		model.addAttribute("topViewedItems", adminStatsService.getTopViewedItems());
		model.addAttribute("statsDays", days);
		model.addAttribute("itemStatusCountsJson", OBJECT_MAPPER.writeValueAsString(adminStatsService.getItemStatusCounts()));
		model.addAttribute("itemCountsByDayJson", OBJECT_MAPPER.writeValueAsString(adminStatsService.getItemCountsByDay(days)));
		model.addAttribute("userSignupCountsByDayJson", OBJECT_MAPPER.writeValueAsString(adminStatsService.getUserSignupCountsByDay(days)));
		model.addAttribute("topViewedItemsJson", OBJECT_MAPPER.writeValueAsString(adminStatsService.getTopViewedItems()));
		log.info("Admin dashboard displayed");
		return "admin/dashboard";
	}

	@GetMapping("/admin/users")
	public String users(Model model, @RequestParam(required = false, defaultValue = "list") String tab) {
		model.addAttribute("users", adminDashboardQueryService.getUsersForDashboard());
		model.addAttribute("blacklistUsers", adminDashboardQueryService.getBlacklistedUsers());
		model.addAttribute("activeTab", "list".equals(tab) ? "list" : "blacklist");
		log.info("Admin users list displayed, tab={}", tab);
		return "admin/users";
	}

	@PostMapping("/admin/users/toggle")
	public String usersToggle(@RequestParam("id") UUID userId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.toggleUserActive(currentUserId, userId);
		ra.addFlashAttribute("message", "ユーザー状態を更新しました");
		return "redirect:/admin/users?tab=list";
	}

	@PostMapping("/admin/users/blacklist/restore")
	public String usersBlacklistRestore(@RequestParam("id") UUID userId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.restoreUser(currentUserId, userId);
		ra.addFlashAttribute("message", "制限を解除しました");
		return "redirect:/admin/users?tab=blacklist";
	}

	@PostMapping("/admin/users/blacklist/delete")
	public String usersBlacklistDelete(@RequestParam("id") UUID userId, RedirectAttributes ra) {
		try {
			UUID currentUserId = SecurityUtil.getCurrentUserId();
			adminUserService.deleteUserPermanently(currentUserId, userId);
			ra.addFlashAttribute("message", "ユーザーを永久削除しました");
		} catch (Exception e) {
			log.warn("User permanent delete failed: {}", e.getMessage());
			ra.addFlashAttribute("error", "削除に失敗しました。出品履歴や取引履歴がある場合は削除できません。");
		}
		return "redirect:/admin/users?tab=blacklist";
	}

	@GetMapping("/admin/products")
	public String products(Model model, @RequestParam(required = false, defaultValue = "list") String tab) {
		model.addAttribute("products", adminDashboardQueryService.getProductsForDashboard());
		model.addAttribute("blacklistProducts", adminDashboardQueryService.getBlacklistedProducts());
		model.addAttribute("activeTab", "list".equals(tab) ? "list" : "blacklist");
		log.info("Admin products list displayed, tab={}", tab);
		return "admin/products";
	}

	@PostMapping("/admin/products/force-delete")
	public String productsForceDelete(@RequestParam("id") UUID itemId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.forceDeleteItem(currentUserId, itemId, "管理画面から強制削除");
		ra.addFlashAttribute("message", "商品を削除しました");
		return "redirect:/admin/products?tab=list";
	}

	@PostMapping("/admin/products/blacklist/restore")
	public String productsBlacklistRestore(@RequestParam("id") UUID itemId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.restoreItem(currentUserId, itemId);
		ra.addFlashAttribute("message", "商品を復元しました");
		return "redirect:/admin/products?tab=blacklist";
	}

	@PostMapping("/admin/products/blacklist/delete")
	public String productsBlacklistDelete(@RequestParam("id") UUID itemId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.deleteItemPermanently(currentUserId, itemId);
		ra.addFlashAttribute("message", "商品を永久削除しました");
		return "redirect:/admin/products?tab=blacklist";
	}

}
