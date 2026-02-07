package com.example.flea_market_app.common.service.impl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.BusinessException;
import com.example.flea_market_app.common.service.S3ImageService;

/**
 * ローカルファイルシステムへの画像保存を行う S3ImageService の実装。
 * 開発時（app.image.storage=local）に使用し、AWS S3 なしで出品・プロフィール画像を利用可能にする。
 */
@Service
@ConditionalOnProperty(name = "app.image.storage", havingValue = "local")
public class LocalFileImageServiceImpl implements S3ImageService {

	private static final Logger log = LoggerFactory.getLogger(LocalFileImageServiceImpl.class);

	@Value("${app.upload.dir:./uploads}")
	private String uploadDir;

	@Override
	public String uploadImage(String bucketName, String s3Key, InputStream inputStream, String contentType, long contentLength) {
		log.info("Uploading image to local storage: key={}, size={}", s3Key, contentLength);

		Path target = Path.of(uploadDir).resolve(s3Key).normalize();
		if (!target.startsWith(Path.of(uploadDir).normalize())) {
			throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "error.image_upload_failed") {};
		}

		try {
			Files.createDirectories(target.getParent());
			Files.copy(inputStream, target);
			log.info("Successfully uploaded image to local storage: {}", s3Key);
			return s3Key;
		} catch (Exception e) {
			log.error("Failed to upload image to local storage: key={}", s3Key, e);
			throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "error.image_upload_failed") {};
		}
	}

	@Override
	public void deleteImage(String bucketName, String s3Key) {
		if (s3Key == null || s3Key.isEmpty()) {
			log.debug("S3 key is null or empty, skipping deletion");
			return;
		}

		Path target = Path.of(uploadDir).resolve(s3Key).normalize();
		if (!target.startsWith(Path.of(uploadDir).normalize())) {
			return;
		}

		try {
			if (Files.deleteIfExists(target)) {
				log.info("Deleted image from local storage: {}", s3Key);
			}
		} catch (Exception e) {
			log.warn("Failed to delete image from local storage: key={}", s3Key, e);
		}
	}

	@Override
	public String generateImageUrl(String bucketName, String s3Key) {
		if (s3Key == null || s3Key.isEmpty()) {
			return "";
		}
		return "/img/" + s3Key;
	}

	@Override
	public String generateProfileImageKey(UUID userId, String fileExtension) {
		return S3ImageService.super.generateProfileImageKey(userId, fileExtension);
	}
}
