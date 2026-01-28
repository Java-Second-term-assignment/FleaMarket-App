package com.example.flea_market_app.catalog.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.catalog.domain.ItemImageEntity;
import com.example.flea_market_app.catalog.repository.ItemImageRepository;
import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.BusinessException;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.common.service.S3ImageService;
import com.example.flea_market_app.common.validation.ImageValidator;

import lombok.RequiredArgsConstructor;

/**
 * 商品画像のアップロード・取得・削除を行うサービスの実装クラス。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ItemImageServiceImpl implements ItemImageService {

	private static final Logger log = LoggerFactory.getLogger(ItemImageServiceImpl.class);

	private final S3ImageService s3ImageService;
	private final ItemImageRepository itemImageRepository;

	@Value("${aws.s3.bucket.name}")
	private String bucketName;

	@Value("${aws.s3.item-image.max-size:5242880}")
	private Long maxImageSize;

	@Value("${aws.s3.item-image.max-count:10}")
	private Integer maxImageCount;

	@Override
	@Transactional
	public List<String> uploadItemImages(UUID itemId, List<MultipartFile> files) {
		log.info("Uploading {} images for item: {}", files != null ? files.size() : 0, itemId);

		if (files == null || files.isEmpty()) {
			log.warn("No images provided for item: {}", itemId);
			return new ArrayList<>();
		}

		// 画像数のバリデーション
		if (files.size() > maxImageCount) {
			log.warn("Image count exceeds maximum: {} > {}", files.size(), maxImageCount);
			throw new ValidationBusinessException(
					ErrorCode.ITEM_IMAGE_COUNT_EXCEEDED,
					"error.item_image_count_exceeded");
		}

		List<String> uploadedS3Keys = new ArrayList<>();

		try {
			// 既存の画像数を取得してdisplay_orderを決定
			long existingImageCount = itemImageRepository.countByItemId(itemId);
			short displayOrder = (short) existingImageCount;

			for (MultipartFile file : files) {
				// 画像バリデーション
				ImageValidator.validate(file, maxImageSize);

				// ファイル拡張子の取得
				String originalFilename = file.getOriginalFilename();
				if (originalFilename == null || originalFilename.isEmpty()) {
					throw new ValidationBusinessException(
							ErrorCode.INVALID_IMAGE_FORMAT,
							"error.invalid_image_format");
				}
				int lastDotIndex = originalFilename.lastIndexOf('.');
				if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
					throw new ValidationBusinessException(
							ErrorCode.INVALID_IMAGE_FORMAT,
							"error.invalid_image_format");
				}
				String fileExtension = originalFilename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);

				// S3キーの生成
				String s3Key = s3ImageService.generateItemImageKey(itemId, fileExtension);

				// S3にアップロード
				try {
					s3ImageService.uploadImage(
							bucketName,
							s3Key,
							file.getInputStream(),
							file.getContentType(),
							file.getSize());
					log.info("Successfully uploaded image to S3: {}", s3Key);
				} catch (IOException e) {
					log.error("Failed to read image file for upload: itemId={}, s3Key={}", itemId, s3Key, e);
					throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "error.image_upload_failed") {
					};
				}

				// データベースに保存
				ItemImageEntity itemImage = new ItemImageEntity();
				itemImage.setId(UUID.randomUUID());
				itemImage.setItemId(itemId);
				itemImage.setS3Key(s3Key);
				itemImage.setContentType(file.getContentType());
				itemImage.setByteSize(file.getSize());
				itemImage.setDisplayOrder(displayOrder);

				itemImageRepository.save(itemImage);
				uploadedS3Keys.add(s3Key);

				log.info("Successfully saved image metadata to database: itemId={}, s3Key={}, displayOrder={}",
						itemId, s3Key, displayOrder);

				displayOrder++;
			}

			log.info("Successfully uploaded {} images for item: {}", uploadedS3Keys.size(), itemId);
			return uploadedS3Keys;

		} catch (BusinessException e) {
			// 既にビジネス例外の場合はそのまま再スロー
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error while uploading images for item: {}", itemId, e);
			// アップロードに失敗した場合は、既にアップロードした画像を削除
			for (String s3Key : uploadedS3Keys) {
				try {
					s3ImageService.deleteImage(bucketName, s3Key);
					log.info("Cleaned up uploaded image after error: {}", s3Key);
				} catch (Exception cleanupException) {
					log.warn("Failed to cleanup image after error: {}", s3Key, cleanupException);
				}
			}
			throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "error.image_upload_failed") {
			};
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getItemImageUrls(UUID itemId) {
		log.debug("Getting image URLs for item: {}", itemId);

		List<ItemImageEntity> images = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId);
		List<String> urls = new ArrayList<>();

		for (ItemImageEntity image : images) {
			String url = s3ImageService.generateImageUrl(bucketName, image.getS3Key());
			urls.add(url);
		}

		log.debug("Found {} images for item: {}", urls.size(), itemId);
		return urls;
	}

	@Override
	@Transactional(readOnly = true)
	public String getThumbnailImageUrl(UUID itemId) {
		log.debug("Getting thumbnail image URL for item: {}", itemId);

		List<ItemImageEntity> images = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId);
		if (images.isEmpty()) {
			log.debug("No images found for item: {}", itemId);
			return null;
		}

		// 1枚目（display_order=0）がサムネイル
		ItemImageEntity thumbnail = images.get(0);
		String url = s3ImageService.generateImageUrl(bucketName, thumbnail.getS3Key());

		log.debug("Found thumbnail image for item: {}, s3Key={}", itemId, thumbnail.getS3Key());
		return url;
	}

	@Override
	@Transactional
	public void deleteItemImages(UUID itemId) {
		log.info("Deleting all images for item: {}", itemId);

		List<ItemImageEntity> images = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId);

		for (ItemImageEntity image : images) {
			try {
				s3ImageService.deleteImage(bucketName, image.getS3Key());
				log.info("Successfully deleted image from S3: {}", image.getS3Key());
			} catch (Exception e) {
				log.warn("Failed to delete image from S3: {}. Continuing with database deletion.", image.getS3Key(), e);
			}
		}

		itemImageRepository.deleteByItemId(itemId);
		log.info("Successfully deleted {} images from database for item: {}", images.size(), itemId);
	}
}
