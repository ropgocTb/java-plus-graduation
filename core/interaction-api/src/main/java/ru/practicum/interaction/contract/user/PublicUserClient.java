package ru.practicum.interaction.contract.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/users", fallback = PublicUserFallback.class)
public interface PublicUserClient extends PublicUserOperations {
}
