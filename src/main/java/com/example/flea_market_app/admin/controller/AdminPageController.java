package com.example.flea_market_app.admin.controller;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.admin.controller.dto.AdminProductRowDto;
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

	@PostMapping("/admin/users/admin-role")
	public String usersAdminRole(
			@RequestParam("id") UUID userId,
			@RequestParam("makeAdmin") boolean makeAdmin,
			@RequestParam(value = "reason", required = false, defaultValue = "管理画面から変更") String reason,
			RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.changeAdminRole(currentUserId, userId, makeAdmin, reason);
		ra.addFlashAttribute("message", makeAdmin ? "管理者権限を付与しました" : "管理者権限を剥奪しました");
		return "redirect:/admin/users?tab=list";
	}

	@PostMapping("/admin/users/update")
	public String usersUpdate(
			@RequestParam("id") UUID userId,
			@RequestParam("name") String displayName,
			@RequestParam("email") String email,
			RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.updateUserProfile(currentUserId, userId, displayName, email);
		ra.addFlashAttribute("message", "ユーザー情報を更新しました");
		return "redirect:/admin/users?tab=list";
	}

	@PostMapping("/admin/users/freeze")
	public String usersFreeze(
			@RequestParam("id") UUID userId,
			@RequestParam("reason") String reason,
			@RequestParam(value = "frozenUntilDays", required = false) Integer frozenUntilDays,
			RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		OffsetDateTime frozenUntil = frozenUntilDays != null
				? OffsetDateTime.now().plusDays(frozenUntilDays)
				: null;
		adminUserService.freezeUser(currentUserId, userId, reason, frozenUntil);
		ra.addFlashAttribute("message", "ユーザーを凍結しました");
		return "redirect:/admin/users?tab=list";
	}

	@PostMapping("/admin/users/force-withdraw")
	public String usersForceWithdraw(
			@RequestParam("id") UUID userId,
			@RequestParam("reason") String reason,
			RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.forceWithdraw(currentUserId, userId, reason);
		ra.addFlashAttribute("message", "ユーザーを強制退会させました");
		return "redirect:/admin/users?tab=list";
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
		List<AdminProductRowDto> violationProducts;
		try {
			violationProducts = adminDashboardQueryService.getViolationProducts();
		} catch (DataAccessException e) {
			log.warn("Violation list unavailable (item_moderation table may be missing): {}", e.getMessage());
			violationProducts = Collections.emptyList();
		}
		model.addAttribute("violationProducts", violationProducts);
		String activeTab = "violations".equals(tab) ? "violations" : "blacklist".equals(tab) ? "blacklist" : "list";
		model.addAttribute("activeTab", activeTab);
		log.info("Admin products list displayed, tab={}", tab);
		return "admin/products";
	}

	@PostMapping("/admin/products/update")
	public String productsUpdate(
			@RequestParam("id") UUID itemId,
			@RequestParam("name") String name,
			@RequestParam("price") long price,
			RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.updateItem(currentUserId, itemId, name, price);
		ra.addFlashAttribute("message", "商品情報を更新しました");
		return "redirect:/admin/products?tab=list";
	}

	@PostMapping("/admin/products/suspend")
	public String productsSuspend(@RequestParam("id") UUID itemId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.suspendItem(currentUserId, itemId);
		ra.addFlashAttribute("message", "商品を出品停止しました");
		return "redirect:/admin/products?tab=list";
	}

	@PostMapping("/admin/products/unsuspend")
	public String productsUnsuspend(@RequestParam("id") UUID itemId, RedirectAttributes ra) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.unsuspendItem(currentUserId, itemId);
		ra.addFlashAttribute("message", "出品停止を解除しました");
		return "redirect:/admin/products?tab=list";
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
