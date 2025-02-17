package com.example.webdisk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${webdisk.thread-core:2}")
    private int THREAD_CORE;

    @Value("${webdisk.thread-max:10}")
    private int THREAD_MAX;

    @Value("${webdisk.thread-queue:30}")
    private int THREAD_QUEUE;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_CORE);
        executor.setMaxPoolSize(THREAD_MAX);
        executor.setQueueCapacity(THREAD_QUEUE);
        executor.setThreadNamePrefix("WebDiskThread-");
        executor.initialize();
        return executor;
    }
}