package com.example.flea_market_app.listing.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.listing.controller.dto.ProductFormDto;
import com.example.flea_market_app.listing.service.ListingService;
import com.example.flea_market_app.listing.service.SellerListingService;
import com.example.flea_market_app.listing.service.dto.CreateItemRequest;
import com.example.flea_market_app.listing.service.dto.CreateItemResponse;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.repository.UserRepository;
import com.example.flea_market_app.user.service.UserRankService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ListingPageController {

	private static final Logger log = LoggerFactory.getLogger(ListingPageController.class);

	private final ProductListService productListService;
	private final ListingService listingService;
	private final SellerListingService sellerListingService;
	private final UserRepository userRepository;
	private final UserRankService userRankService;

	@GetMapping({ "/product/add", "/items/add" })
	public String productAddPage(Model model) {
		ProductFormDto form = new ProductFormDto();
		form.setCondition("USED_GOOD");
		form.setShippingFeePayer("SELLER");

		model.addAttribute("productForm", form);
		model.addAttribute("categories", productListService.getCategories());
		addCommissionAttributes(model, SecurityUtil.getCurrentUserId(), null);
		log.info("Product add page displayed");
		return "item/product_add";
	}

	@PostMapping("/product/submit")
	public String productSubmit(
			@Valid ProductFormDto form,
			BindingResult bindingResult,
			@RequestPart(value = "images", required = false) List<MultipartFile> images,
			Model model,
			RedirectAttributes redirectAttributes) {
		List<MultipartFile> imageList = images != null ? images : new ArrayList<>();
		if (imageList.isEmpty() || imageList.stream().allMatch(f -> f == null || f.isEmpty())) {
			bindingResult.reject("images.required", "画像を1枚以上選択してください");
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("productForm", form);
			model.addAttribute("categories", productListService.getCategories());
			addCommissionAttributes(model, SecurityUtil.getCurrentUserId(), form.getPrice());
			if (bindingResult.hasGlobalErrors()) {
				model.addAttribute("errorMessage", bindingResult.getGlobalErrors().get(0).getDefaultMessage());
			}
			return "item/product_add";
		}
		List<MultipartFile> validImages = imageList.stream()
				.filter(f -> f != null && !f.isEmpty())
				.toList();
		CreateItemRequest request = toCreateItemRequest(form);

		CreateItemResponse response = listingService.createItem(SecurityUtil.getCurrentUserId(), request, validImages);
		log.info("Product created successfully: itemId={}, name={}", response.getItemId(), form.getName());
		redirectAttributes.addFlashAttribute("successMessage", "出品が完了しました");
		return "redirect:/products/" + response.getItemId();
	}

	@PostMapping("/product/{id}/suspend")
	public String suspendProduct(@PathVariable("id") UUID id, RedirectAttributes ra) {
		UUID userId = SecurityUtil.getCurrentUserId();
		sellerListingService.suspend(userId, id);
		ra.addFlashAttribute("successMessage", "出品を停止しました");
		return "redirect:/products/" + id;
	}

	@PostMapping("/product/{id}/unsuspend")
	public String unsuspendProduct(@PathVariable("id") UUID id, RedirectAttributes ra) {
		UUID userId = SecurityUtil.getCurrentUserId();
		sellerListingService.unsuspend(userId, id);
		ra.addFlashAttribute("successMessage", "出品を再開しました");
		return "redirect:/products/" + id;
	}

	@PostMapping("/product/{id}/delete")
	public String deleteProduct(@PathVariable("id") UUID id, RedirectAttributes ra) {
		UUID userId = SecurityUtil.getCurrentUserId();
		sellerListingService.delete(userId, id);
		ra.addFlashAttribute("message", "商品を削除しました");
		return "redirect:/user/settings";
	}

	private void addCommissionAttributes(Model model, java.util.UUID userId, Long priceAmount) {
		if (userId == null) {
			model.addAttribute("commissionRatePercent", 10);
			model.addAttribute("fee", null);
			return;
		}
		UserEntity user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			model.addAttribute("commissionRatePercent", 10);
			model.addAttribute("fee", null);
			return;
		}
		UserRank rank = userRankService.loadRank(user.getUserRankId());
		int bps = rank.getCommissionBps();
		model.addAttribute("commissionRatePercent", bps / 100);
		Long fee = null;
		if (priceAmount != null && priceAmount > 0) {
			fee = priceAmount * bps / 10000;
		}
		model.addAttribute("fee", fee);
	}

	private CreateItemRequest toCreateItemRequest(ProductFormDto form) {
		CreateItemRequest request = new CreateItemRequest();
		request.setName(form.getName());
		request.setDescription(form.getDescription() != null ? form.getDescription() : "");
		request.setPriceAmount(form.getPrice());
		request.setCategoryId(form.getCategoryId());
		request.setCondition(form.getCondition());
		request.setShippingFeePayer(form.getShippingFeePayer());
		return request;
	}
}
