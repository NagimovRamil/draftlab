package com.draftlab.payment.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration
public class AsyncConfig {
    @Bean
    Executor ledgerExecutor() {
        var executor = new SimpleAsyncTaskExecutor("ledger-");
        executor.setVirtualThreads(true);
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return executor;
    }
}
