package com.example.flea_market_app.common.exception;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.example.flea_market_app.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * アプリケーション全体の例外をハンドリングするグローバル例外ハンドラー。
 * 
 * <p>BusinessExceptionのみをハンドリングします。その他の例外（RuntimeException、
 * NullPointerException、外部API例外など）は意図的に未処理とし、Springの
 * デフォルトエラーハンドリングに委譲します。
 * 
 * <p>Accept: text/html のリクエストの場合はHTMLエラーページを返し、
 * それ以外（APIクライアント）の場合はJSONレスポンスを返します。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String FALLBACK_MESSAGE_KEY = "error.fallback";
	// 最終防衛線: messages自体が壊れている状況での最後の逃げ道
	private static final String ULTIMATE_FALLBACK_MESSAGE = "An error occurred";

	private final MessageSource messageSource;

	/**
	 * BusinessExceptionをハンドリングします。
	 * 
	 * <p>Accept に text/html が含まれる場合はHTMLエラーページを返し、
	 * それ以外はJSONを返します。
	 * 
	 * @param ex 発生した例外
	 * @param request リクエスト
	 * @param locale リクエストのロケール
	 * @return 適切なHTTPステータスとErrorResponse、またはModelAndView
	 */
	@ExceptionHandler(BusinessException.class)
	public Object handleBusinessException(
			BusinessException ex,
			HttpServletRequest request,
			Locale locale) {

		HttpStatus httpStatus = ex.getErrorCode().getHttpStatus();

		logException(ex, httpStatus);

		if (acceptsHtml(request)) {
			String message = getLocalizedMessage(ex.getMessageKey(), resolveLocale(locale));
			ModelAndView mav = new ModelAndView();
			mav.setStatus(httpStatus);
			mav.addObject("status", httpStatus.value());
			mav.addObject("message", message);
			if (httpStatus == HttpStatus.NOT_FOUND) {
				mav.setViewName("error/404");
			} else {
				mav.setViewName("error/error");
			}
			return mav;
		}

		return buildResponse(ex, locale, httpStatus);
	}

	private boolean acceptsHtml(HttpServletRequest request) {
		String accept = request.getHeader("Accept");
		return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
	}

	private void logException(BusinessException ex, HttpStatus httpStatus) {
		String message = "Business exception: errorCode={}, messageKey={}, httpStatus={}";
		Object[] args = { ex.getErrorCode(), ex.getMessageKey(), httpStatus.value() };
		
		if (httpStatus.is5xxServerError()) {
			log.error(message, args, ex);
		} else if (httpStatus.is4xxClientError()) {
			log.warn(message, args);
		} else {
			log.info(message, args);
		}
	}

	private ResponseEntity<ErrorResponse> buildResponse(
			BusinessException ex,
			Locale locale,
			HttpStatus httpStatus) {
		
		Locale resolvedLocale = resolveLocale(locale);
		String message = getLocalizedMessage(ex.getMessageKey(), resolvedLocale);
		ErrorResponse errorResponse = ErrorResponse.of(
				ex.getErrorCode().getApiCode(),
				message);
		
		return ResponseEntity.status(httpStatus).body(errorResponse);
	}

	private Locale resolveLocale(Locale locale) {
		return locale != null ? locale : Locale.getDefault();
	}

	private String getLocalizedMessage(String messageKey, Locale locale) {
		Locale resolvedLocale = resolveLocale(locale);
		try {
			return messageSource.getMessage(
					messageKey,
					null,
					resolvedLocale);
		} catch (NoSuchMessageException e) {
			log.warn("Message not found for key: {}, locale: {}", messageKey, resolvedLocale);
			return getFallbackMessage(resolvedLocale);
		} catch (Exception e) {
			log.error("Failed to get localized message for key: {}", messageKey, e);
			return getFallbackMessage(resolvedLocale);
		}
	}

	private String getFallbackMessage(Locale locale) {
		Locale resolvedLocale = resolveLocale(locale);
		try {
			return messageSource.getMessage(
					FALLBACK_MESSAGE_KEY,
					null,
					ULTIMATE_FALLBACK_MESSAGE,
					resolvedLocale);
		} catch (Exception e) {
			log.error("Failed to get fallback message", e);
			return ULTIMATE_FALLBACK_MESSAGE;
		}
	}

}
