package com.lifeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LifeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeShopApplication.class, args);
    }
}
