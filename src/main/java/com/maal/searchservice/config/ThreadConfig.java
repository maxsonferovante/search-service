package com.maal.searchservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Configuration
public class ThreadConfig {

    @Bean(name = "virtualThreadTaskExecutor")
    public ExecutorService virtualThreadTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "apiAccessSemaphore")
    public Semaphore apiAccessSemaphore() {
        // Cria um semáforo com 10 permissões, permitindo 10 acessos concorrentes
        return new Semaphore(10); // Limite de 10 tarefas concorrentes
    }
}
