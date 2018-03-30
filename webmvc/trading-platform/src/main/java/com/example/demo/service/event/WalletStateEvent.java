package com.example.demo.service.event;

import java.security.Principal;

import com.example.demo.controller.ws.Message;
import lombok.Value;

import org.springframework.context.ApplicationEvent;

@Value
public class WalletStateEvent extends ApplicationEvent {

	private final Principal user;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public WalletStateEvent(Message<Float> source, Principal user) {
		super(source);
		this.user = user;
	}
}
