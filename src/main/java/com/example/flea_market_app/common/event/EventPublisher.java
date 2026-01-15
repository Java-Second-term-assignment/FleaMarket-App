package com.example.flea_market_app.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

	@Autowired
	private ApplicationEventPublisher publisher;

	public void publish(DomainEvent event) {
		publisher.publishEvent(event);
	}

}
