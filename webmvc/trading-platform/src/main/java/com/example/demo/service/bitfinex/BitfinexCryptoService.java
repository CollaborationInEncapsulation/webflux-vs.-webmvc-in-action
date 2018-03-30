package com.example.demo.service.bitfinex;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.annotation.PreDestroy;

import com.example.demo.service.event.CryptoMarketsEvent;
import com.example.demo.service.CryptoService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Slf4j
@Service
public class BitfinexCryptoService implements CryptoService {
	private static final String SERVICE_URI = "wss://api.bitfinex.com/ws/2";

	private final WebSocketSession session;

	public BitfinexCryptoService(ApplicationContext context) throws ExecutionException, InterruptedException {
		StandardWebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
		this.session = simpleWebSocketClient.doHandshake(
				new BitfinexWebSocketHandler(m -> context.publishEvent(new CryptoMarketsEvent(m))),
				SERVICE_URI)
		.get();
	}

	@PreDestroy
	public void cleanUp() throws IOException {
		session.close();
	}
}
