package ru.practicum.interaction.contract.subscription;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "subscription-service", path = "/users")
public interface SubscriptionClient extends SubscriptionOperations {
}
