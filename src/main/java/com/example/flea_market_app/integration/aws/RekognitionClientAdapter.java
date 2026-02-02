package com.example.flea_market_app.integration.aws;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.common.exception.RetryableExternalException;
import com.example.flea_market_app.config.aws.AwsProperties;
import com.example.flea_market_app.integration.aws.dto.ImageModerationResult;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;
import software.amazon.awssdk.services.rekognition.model.S3Object;

@Component
public class RekognitionClientAdapter implements RekognitionClientPort {

	private final RekognitionClient client;
	private final AwsProperties props;

	public RekognitionClientAdapter(RekognitionClient client, AwsProperties props) {
		this.client = client;
		this.props = props;
	}

	@Override
	public ImageModerationResult analyzeImage(String s3ObjectKey) {
		try {
			var s3 = S3Object.builder()
					.bucket(props.s3Bucket())
					.name(s3ObjectKey)
					.build();

			var img = Image.builder()
					.s3Object(s3)
					.build();

			var req = DetectModerationLabelsRequest.builder()
					.image(img)
					.minConfidence(70f)
					.build();

			var res = client.detectModerationLabels(req);

			boolean adult = false;
			boolean violence = false;
			List<String> reasons = new ArrayList<>();
			double max = 0.0;

			for (ModerationLabel label : res.moderationLabels()) {
				String name = label.name(); // "Explicit Nudity", "Violence" など
				double conf = label.confidence() == null ? 0.0 : (label.confidence() / 100.0);
				max = Math.max(max, conf);
				reasons.add(name + ":" + label.confidence());

				if (name.toLowerCase().contains("nudity"))
					adult = true;
				if (name.toLowerCase().contains("violence"))
					violence = true;
			}

			return new ImageModerationResult(adult, violence, max, reasons);

		} catch (SdkClientException e) {
			throw new RetryableExternalException(ErrorCode.EXTERNAL_SERVICE_TEMPORARY,
					"aws.rekognition.temporary_failure",
					e);
		} catch (Exception e) {
			throw new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_FAILED,
					"aws.rekognition.failed",
					e);
		}
	}
}
