package com.example.flea_market_app.common.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

	/* 
	 * イベントの発生を記録するだけ
	 * 時間の値(不変)を取得するだけにとどめたいから今回はInstanceを採用
	 */

	UUID eventId();

	Instant occurredAt();
}
