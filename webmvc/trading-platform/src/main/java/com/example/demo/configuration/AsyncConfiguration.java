package com.example.demo.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfiguration extends AsyncConfigurerSupport {

	@Override
	public Executor getAsyncExecutor() {
		return ForkJoinPool.commonPool();
	}
}
