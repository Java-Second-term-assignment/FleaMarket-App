package com.example.flea_market_app.catalog.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.admin.domain.ReportType;
import com.example.flea_market_app.admin.domain.TargetType;
import com.example.flea_market_app.admin.service.ReportService;
import com.example.flea_market_app.catalog.controller.dto.ReviewForm;
import com.example.flea_market_app.catalog.service.ItemViewService;
import com.example.flea_market_app.catalog.service.ProductListService.ProductListResult;
import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.catalog.service.ProductReviewQueryService;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.engagement.favorite.repository.FavoriteRepository;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.ReviewRating;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.transaction.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductPageController {

	private static final Logger log = LoggerFactory.getLogger(ProductPageController.class);

	private final ProductListService productListService;
	private final ItemViewService itemViewService;
	private final FavoriteRepository favoriteRepository;
	private final ProductReviewQueryService productReviewQueryService;
	private final OrderRepository orderRepository;
	private final ReviewService reviewService;
	private final ReportService reportService;

	@Value("${app.product-list.page-size:50}")
	private int defaultPageSize;

	private static final int MAX_PAGE_SIZE = 100;

	@GetMapping({ "/", "/products" })
	public String productList(
			Optional<String> keyword,
			Optional<String> category,
			Optional<String> sort,
			Optional<Integer> page,
			Optional<Integer> size,
			Model model) {
		Optional<UUID> categoryId = category.filter(s -> !s.isBlank())
				.flatMap(s -> parseUuid(s));

		int pageIndex = page.filter(p -> p >= 0).orElse(0);
		int pageSize = size.filter(s -> s >= 1).map(s -> Math.min(MAX_PAGE_SIZE, s)).orElse(defaultPageSize);

		ProductListResult result = productListService.getProducts(
				keyword, categoryId, sort.orElse("new"), pageIndex, pageSize);

		model.addAttribute("products", result.products());
		model.addAttribute("categories", productListService.getCategories());
		model.addAttribute("rankings", productListService.getRankings());
		model.addAttribute("totalPages", result.totalPages());
		model.addAttribute("currentPage", result.currentPage());
		model.addAttribute("pageSize", result.pageSize());
		log.info("Product list displayed: keyword={}, category={}, page={}", keyword.orElse(null), category.orElse(null), pageIndex);

		return "item/product_list";
	}

	@GetMapping("/products/{id}")
	public String productDetail(@PathVariable UUID id, Model model) {
		log.info("Product detail requested: itemId={}", id);
		return productListService.getProductDetail(id)
				.map(product -> {
					SecurityUtil.getCurrentUserIdOptional()
							.ifPresent(userId -> itemViewService.recordViewIfNew(userId, id));
					boolean isFavorited = SecurityUtil.getCurrentUserIdOptional()
							.map(userId -> favoriteRepository.existsByUserIdAndItemId(userId, id))
							.orElse(false);
					model.addAttribute("product", product);
					model.addAttribute("isFavorited", isFavorited);
					model.addAttribute("reviewSummary", productReviewQueryService.getReviewSummary(id));
					model.addAttribute("reviews", productReviewQueryService.getReviewsForItem(id));
					model.addAttribute("reviewForm", new ReviewForm());
					model.addAttribute("relatedProducts", productListService.getRelatedProducts(product.getId(), 8));
					return "item/product_detail";
				})
				.orElseThrow(() -> NotFoundBusinessException.of(com.example.flea_market_app.common.exception.ResourceType.ITEM));
	}

	private Optional<UUID> parseUuid(String s) {
		try {
			return Optional.of(UUID.fromString(s));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	@PostMapping("/product/{id}/review")
	public String productReviewPost(
			@PathVariable("id") UUID itemId,
			@Valid @ModelAttribute ReviewForm form,
			BindingResult bindingResult,
			RedirectAttributes ra) {
		UUID userId = SecurityUtil.getCurrentUserId();

		OrderEntity order = orderRepository.findByItemIdAndStatus(itemId, "COMPLETED")
				.orElse(null);
		if (order == null) {
			ra.addFlashAttribute("errorMessage", "この商品の取引を完了していないためレビューできません。");
			return "redirect:/products/" + itemId;
		}
		if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
			ra.addFlashAttribute("errorMessage", "この商品の取引に参加していないためレビューできません。");
			return "redirect:/products/" + itemId;
		}

		if (bindingResult.hasErrors()) {
			ra.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", bindingResult);
			ra.addFlashAttribute("reviewForm", form);
			return "redirect:/products/" + itemId;
		}

		ReviewRating rating = "GOOD".equals(form.getRating()) ? ReviewRating.GOOD : ReviewRating.BAD;
		String comment = form.getComment() != null ? form.getComment().trim() : "";
		reviewService.submitReview(order.getId(), userId, rating, comment);

		ra.addFlashAttribute("message", "レビューを投稿しました。");
		return "redirect:/products/" + itemId;
	}

	@PostMapping("/products/{id}/report")
	public String reportProduct(
			@PathVariable("id") UUID itemId,
			@RequestParam(name = "reportType", required = false) String reportTypeStr,
			@RequestParam(name = "description", required = false) String description,
			RedirectAttributes ra) {
		UUID reporterId = SecurityUtil.getCurrentUserId();
		ReportType reportType;
		try {
			if (reportTypeStr == null || reportTypeStr.isBlank()) {
				ra.addFlashAttribute("reportErrorMessage", "通報理由を選択してください。");
				return "redirect:/products/" + itemId;
			}
			reportType = ReportType.valueOf(reportTypeStr.trim());
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("reportErrorMessage", "通報理由が不正です。");
			return "redirect:/products/" + itemId;
		}
		try {
			reportService.submitReport(reporterId, TargetType.ITEM, itemId, reportType, description);
		} catch (ValidationBusinessException e) {
			ra.addFlashAttribute("reportErrorMessage", e.getMessage());
			return "redirect:/products/" + itemId;
		}
		ra.addFlashAttribute("reportSuccessMessage", "通報を受け付けました。ご協力ありがとうございます。");
		return "redirect:/products/" + itemId;
	}
}
