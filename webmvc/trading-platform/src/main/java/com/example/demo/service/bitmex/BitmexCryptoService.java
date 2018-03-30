package com.example.demo.service.bitmex;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.annotation.PreDestroy;

import com.example.demo.service.CryptoService;
import com.example.demo.service.event.CryptoMarketsEvent;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Slf4j
@Service
public class BitmexCryptoService implements CryptoService {
    private static final String SERVICE_URI = "wss://www.bitmex.com/realtime?subscribe=instrument:XBTUSD,trade:XBTUSD";

    private final WebSocketSession session;

    public BitmexCryptoService(ApplicationContext context) throws ExecutionException, InterruptedException {
        StandardWebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
        this.session = simpleWebSocketClient.doHandshake(
                new BitmexWebSocketHandler(m -> context.publishEvent(new CryptoMarketsEvent(m))),
                SERVICE_URI)
        .get();
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        session.close();
    }
}
