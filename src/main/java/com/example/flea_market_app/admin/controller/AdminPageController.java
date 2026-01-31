package com.example.flea_market_app.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

	private static final Logger log = LoggerFactory.getLogger(AdminPageController.class);

	@GetMapping("/admin/dashboard")
	public String dashboard() {
		log.info("Admin dashboard displayed");
		return "admin/dashboard";
	}

	@GetMapping("/admin/blacklist")
	public String blacklist() {
		log.info("Admin blacklist displayed");
		return "admin/blacklist";
	}
}
