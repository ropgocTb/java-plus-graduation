package ru.practicum.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum")
@EnableFeignClients(basePackages = "ru.practicum")
public class SubscriptionServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApp.class, args);
    }
}
