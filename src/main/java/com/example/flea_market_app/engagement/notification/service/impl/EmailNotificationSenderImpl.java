package com.example.flea_market_app.engagement.notification.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.engagement.notification.service.EmailNotificationSender;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailNotificationSenderImpl implements EmailNotificationSender {

	private static final Logger log = LoggerFactory.getLogger(EmailNotificationSenderImpl.class);

	private final JavaMailSender mailSender;
	private final AuthUserRepository authUserRepository;

	@Value("${spring.mail.from:noreply@example.com}")
	private String fromAddress;

	@Override
	@Transactional(readOnly = true)
	public void sendTransactionEstablished(UUID sellerUserId, UUID orderId) {
		Optional<AuthUserEntity> authOpt = authUserRepository.findByUserId(sellerUserId);
		if (authOpt.isEmpty()) {
			log.warn("Cannot send transaction-established email: no auth user for seller userId={}", sellerUserId);
			return;
		}
		String to = authOpt.get().getEmail();
		String subject = "取引が成立しました";
		String body = "取引が成立しました。注文ID: " + orderId + "\n\nアプリでご確認ください。";
		send(to, subject, body, "transactionEstablished", orderId);
	}

	@Override
	@Transactional(readOnly = true)
	public void sendChatReceived(UUID recipientUserId, UUID orderId) {
		Optional<AuthUserEntity> authOpt = authUserRepository.findByUserId(recipientUserId);
		if (authOpt.isEmpty()) {
			log.warn("Cannot send chat-received email: no auth user for recipient userId={}", recipientUserId);
			return;
		}
		String to = authOpt.get().getEmail();
		String subject = "チャットが届きました";
		String body = "取引に関するチャットが届きました。注文ID: " + orderId + "\n\nアプリでご確認ください。";
		send(to, subject, body, "chatReceived", orderId);
	}

	private void send(String to, String subject, String body, String kind, UUID orderId) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromAddress);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
			log.info("Sent {} email for orderId={} to {}", kind, orderId, to);
		} catch (Exception e) {
			log.warn("Failed to send {} email for orderId={} to {}: {}", kind, orderId, to, e.getMessage());
		}
	}
}
