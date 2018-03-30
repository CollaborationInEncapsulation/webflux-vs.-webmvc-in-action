package com.example.demo.service;

import com.example.demo.controller.ws.Message;

public interface CryptoService {

	default void trade(Message<Message.Trade> trade) { }
}

