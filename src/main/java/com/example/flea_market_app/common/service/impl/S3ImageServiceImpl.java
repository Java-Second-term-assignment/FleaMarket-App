package com.example.flea_market_app.common.service.impl;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.BusinessException;
import com.example.flea_market_app.common.service.S3ImageService;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * S3への画像アップロード・削除・URL生成を行うサービスの実装クラス。
 * 
 * <p>S3ImageServiceインターフェースの実装です。
 * AWS SDK for Java 2.xを使用してS3操作を行います。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Service
public class S3ImageServiceImpl implements S3ImageService {

	private static final Logger log = LoggerFactory.getLogger(S3ImageServiceImpl.class);

	private final S3Client s3Client;

	@Value("${aws.s3.region:ap-northeast-1}")
	private String region;

	/**
	 * コンストラクタ。
	 * 
	 * @param s3Client S3Clientインスタンス
	 */
	public S3ImageServiceImpl(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	@Override
	public String uploadImage(String bucketName, String s3Key, InputStream inputStream, String contentType, long contentLength) {
		log.info("Uploading image to S3: bucket={}, key={}, size={}", bucketName, s3Key, contentLength);

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(s3Key)
					.contentType(contentType)
					.contentLength(contentLength)
					.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

			log.info("Successfully uploaded image to S3: bucket={}, key={}", bucketName, s3Key);
			return s3Key;
		} catch (S3Exception e) {
			log.error("Failed to upload image to S3: bucket={}, key={}, errorCode={}", bucketName, s3Key, e.awsErrorDetails().errorCode(), e);
			throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "error.image_upload_failed") {
			};
		} catch (Exception e) {
			log.error("Unexpected error while uploading image to S3: bucket={}, key={}", bucketName, s3Key, e);
			throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "error.image_upload_failed") {
			};
		}
	}

	@Override
	public void deleteImage(String bucketName, String s3Key) {
		if (s3Key == null || s3Key.isEmpty()) {
			log.debug("S3 key is null or empty, skipping deletion");
			return;
		}

		log.info("Deleting image from S3: bucket={}, key={}", bucketName, s3Key);

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
					.bucket(bucketName)
					.key(s3Key)
					.build();

			s3Client.deleteObject(deleteObjectRequest);

			log.info("Successfully deleted image from S3: bucket={}, key={}", bucketName, s3Key);
		} catch (S3Exception e) {
			log.warn("Failed to delete image from S3: bucket={}, key={}, errorCode={}. Continuing execution.", bucketName, s3Key, e.awsErrorDetails().errorCode(), e);
			// 削除失敗は警告ログを記録するが、処理は継続
			throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED, "error.image_delete_failed") {
			};
		} catch (Exception e) {
			log.warn("Unexpected error while deleting image from S3: bucket={}, key={}. Continuing execution.", bucketName, s3Key, e);
			// 削除失敗は警告ログを記録するが、処理は継続
			throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED, "error.image_delete_failed") {
			};
		}
	}

	@Override
	public String generateImageUrl(String bucketName, String s3Key) {
		Objects.requireNonNull(bucketName, "bucketName");
		Objects.requireNonNull(s3Key, "s3Key");
		// シンプルな公開URL形式: https://{bucket}.s3.{region}.amazonaws.com/{key}
		// 注意: 実際の環境では、CloudFrontや署名付きURLを使用することを推奨
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
	}

	@Override
	public String generateProfileImageKey(UUID userId, String fileExtension) {
		return S3ImageService.super.generateProfileImageKey(userId, fileExtension);
	}

}
