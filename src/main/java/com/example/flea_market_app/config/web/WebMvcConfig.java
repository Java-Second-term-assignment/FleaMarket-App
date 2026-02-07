package com.example.flea_market_app.config.web;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 設定。
 * CORS は CorsConfig（CorsConfigurationSource）に一本化している。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${app.upload.dir:./uploads}")
	private String uploadDir;

	@Value("${app.demo-images.dir:./picture}")
	private String demoImagesDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path base = Path.of(uploadDir).toAbsolutePath().normalize();
		String itemsLocation = "file:" + base.resolve("items") + "/";
		String profilesLocation = "file:" + base.resolve("profiles") + "/";
		registry.addResourceHandler("/img/items/**").addResourceLocations(itemsLocation);
		registry.addResourceHandler("/img/profiles/**").addResourceLocations(profilesLocation);

		Path demoBase = Path.of(demoImagesDir).toAbsolutePath().normalize();
		String demoLocation = "file:" + demoBase + "/";
		registry.addResourceHandler("/img/demo/**").addResourceLocations(demoLocation);
	}
}