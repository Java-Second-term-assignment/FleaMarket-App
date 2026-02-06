package com.example.flea_market_app.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 設定。
 * CORS は CorsConfig（CorsConfigurationSource）に一本化している。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}