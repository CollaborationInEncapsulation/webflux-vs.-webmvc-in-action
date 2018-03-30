package com.example.demo.controller.ws;

import java.security.Principal;
import java.util.List;

import com.example.demo.service.CryptoService;
import com.example.demo.service.WalletService;
import com.example.demo.service.event.CryptoMarketsEvent;
import com.example.demo.service.event.WalletStateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.DESTINATION_HEADER;

@Slf4j
@Controller
@AllArgsConstructor
public class CryptoChannel {

	private final List<CryptoService>           cryptoServices;
	private final ObjectProvider<WalletService> walletServiceProvider;
	private final SimpMessagingTemplate         template;

	@EventListener
	public void doOnSubscribeEvent(SessionSubscribeEvent event) {
		Principal user = event.getUser();
		SecurityContextHolder.getContext()
		                     .setAuthentication((Authentication) user);

		if (event.getMessage()
		         .getHeaders()
		         .get(DESTINATION_HEADER)
		         .equals("/user/stream")) {

			walletServiceProvider.getIfAvailable().subscribe();
		}
	}

	@EventListener
	@Async
	public void doOnCryptoMarketsEvent(CryptoMarketsEvent event) {
		template.convertAndSend("/stream", new GenericMessage<>(event.getSource()));
	}

	@EventListener
	@Async
	public void doOnWalletStateEvent(WalletStateEvent event) {
		template.convertAndSendToUser(event.getUser().getName(),
				"/stream",
				new GenericMessage<>(event.getSource()));
	}

	@MessageMapping("/")
	public void doTrade(@Payload Message<Message.Trade> message, Principal user) {
		SecurityContextHolder.getContext()
		                     .setAuthentication((Authentication) user);
		cryptoServices.forEach(cs -> cs.trade(message));
	}
}
