package com.example.flea_market_app.catalog.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.flea_market_app.catalog.service.ItemViewService;
import com.example.flea_market_app.catalog.service.ProductListService.ProductListResult;
import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.config.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductPageController {

	private static final Logger log = LoggerFactory.getLogger(ProductPageController.class);

	private final ProductListService productListService;
	private final ItemViewService itemViewService;

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
					model.addAttribute("product", product);
					model.addAttribute("reviewSummary", new ReviewSummaryStub());
					model.addAttribute("reviews", List.<ReviewStub>of());
					model.addAttribute("reviewForm", new com.example.flea_market_app.catalog.controller.dto.ReviewFormStub());
					model.addAttribute("ratings", List.of(1, 2, 3, 4, 5));
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

	private record ReviewSummaryStub(double average, String stars, int count) {
		ReviewSummaryStub() {
			this(0.0, "★★★★☆", 0);
		}
	}

	private record ReviewStub(String userName, Object createdAt, String stars, String comment) {
	}
}
