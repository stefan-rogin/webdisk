package com.example.webdisk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for setting up asynchronous processing in the application.
 * This class defines a thread pool task executor with configurable core pool size,
 * maximum pool size, and queue capacity.
 * 
 * <p>Annotations:</p>
 * <ul>
 *   <li>{@link Configuration}: Indicates that this class declares one or more {@link Bean} methods.</li>
 *   <li>{@link EnableAsync}: Enables Spring's asynchronous method execution capability.</li>
 * </ul>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${webdisk.thread-core:2}")
    private int THREAD_CORE;

    @Value("${webdisk.thread-max:10}")
    private int THREAD_MAX;

    @Value("${webdisk.thread-queue:30}")
    private int THREAD_QUEUE;

    /**
     * Configures and provides a ThreadPoolTaskExecutor bean.
     * 
     * This executor is used to manage asynchronous tasks in the application.
     * It sets the core pool size, maximum pool size, and queue capacity for the thread pool.
     * The threads created by this executor will have the prefix "WebDiskThread-".
     * 
     * @return an Executor configured with the specified thread pool settings.
     */
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