package com.example.flea_market_app.common.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtil {

	/*
	 *  InstantではなくClockを採用する理由
	 *  Instant：今の時刻そのものを取得
	 *  Clock：現在時刻をテストや本番ごとに検証したいから
	 *  
	 */

	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Tokyo");
	private static Clock clock = Clock.system(ZONE_ID);

	public static LocalDateTime now() {
		return LocalDateTime.now(clock);
	}

	// テスト用コード
	public static void setClock(Clock testClock) {

		if (testClock == null) {

			throw new IllegalArgumentException("Clock ust not be null");

		}

		clock = testClock;
	}

	// テスト後の初期化
	public static void resetClock() {
		clock = Clock.system(ZONE_ID);
	}

}
