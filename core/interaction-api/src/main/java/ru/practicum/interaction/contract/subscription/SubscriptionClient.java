package ru.practicum.interaction.contract.subscription;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "subscription-service", path = "/users", fallback = SubscriptionFallback.class)
public interface SubscriptionClient extends SubscriptionOperations {
}
