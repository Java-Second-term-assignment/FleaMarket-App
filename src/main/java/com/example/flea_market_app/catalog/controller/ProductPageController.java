package com.example.flea_market_app.catalog.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductPageController {

	private static final Logger log = LoggerFactory.getLogger(ProductPageController.class);

	private final ProductListService productListService;

	@GetMapping({ "/", "/products" })
	public String productList(
			Optional<String> keyword,
			Optional<String> category,
			Optional<String> sort,
			Model model) {
		Optional<UUID> categoryId = category.filter(s -> !s.isBlank())
				.flatMap(s -> parseUuid(s));

		model.addAttribute("products", productListService.getProducts(keyword, categoryId, sort.orElse("new")));
		model.addAttribute("categories", productListService.getCategories());
		model.addAttribute("rankings", productListService.getRankings());
		log.info("Product list displayed: keyword={}, category={}", keyword.orElse(null), category.orElse(null));

		return "item/product_list";
	}

	@GetMapping("/products/{id}")
	public String productDetail(@PathVariable UUID id, Model model) {
		log.info("Product detail requested: itemId={}", id);
		return productListService.getProductDetail(id)
				.map(product -> {
					model.addAttribute("product", product);
					model.addAttribute("reviewSummary", new ReviewSummaryStub());
					model.addAttribute("reviews", List.<ReviewStub>of());
					model.addAttribute("reviewForm", new com.example.flea_market_app.catalog.controller.dto.ReviewFormStub());
					model.addAttribute("ratings", List.of(1, 2, 3, 4, 5));
					model.addAttribute("relatedProducts", List.of());
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
