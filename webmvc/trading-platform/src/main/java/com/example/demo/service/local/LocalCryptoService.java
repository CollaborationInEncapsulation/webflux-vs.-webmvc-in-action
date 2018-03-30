package com.example.demo.service.local;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

import com.example.demo.controller.ws.Message;
import com.example.demo.domain.Trade;
import com.example.demo.domain.Wallet;
import com.example.demo.repository.TradesRepository;
import com.example.demo.service.CryptoService;
import com.example.demo.service.WalletService;
import com.example.demo.service.event.CryptoMarketsEvent;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalCryptoService implements CryptoService {

    private final TradesRepository tradesRepository;
    private final ObjectProvider<WalletService> walletServiceProvider;
    private final ApplicationContext context;

    @Override
    @PreAuthorize("isAuthenticated()")
    public void trade(Message<Message.Trade> trade) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();
        WalletService walletService = walletServiceProvider.getIfAvailable();
        ForkJoinPool.commonPool()
                    .execute(() -> {
                        SecurityContextHolder.getContext()
                                             .setAuthentication(authentication);
                        try {
                            walletService.withdraw(trade);
                            doTrade(trade);
                            walletService.adjust(trade);
                            Trade storedTrade = doStoreTrade(trade);

                            context.publishEvent(new CryptoMarketsEvent(LocalMessageMapper.tradeToMessage(
                                    storedTrade)));
                        }
                        catch (Wallet.NotEnoughMoneyException e) {
                            // do nothing
                        }
                        catch (Exception e) {
                            walletService.rollback(trade);
                        }
                    });
    }

    private void doTrade(Message<Message.Trade> trade) {
        int wait = ThreadLocalRandom.current()
                                 .nextInt(2000);
        if(wait > 1000) {
            try {
                Thread.sleep(1000);

                throw new RuntimeException();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Thread.sleep(wait);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Trade doStoreTrade(Message<Message.Trade> tradeMessage) {
        return tradesRepository.save(LocalMessageMapper.messageToTrade(tradeMessage));
    }
}
