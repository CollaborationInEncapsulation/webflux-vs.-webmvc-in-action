package com.example.demo.service.event;

import com.example.demo.controller.ws.Message;

import org.springframework.context.ApplicationEvent;


public class CryptoMarketsEvent extends ApplicationEvent {

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public CryptoMarketsEvent(Message<?> source) {
		super(source);
	}
}
