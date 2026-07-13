package ru.practicum.interaction.contract.request;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/users", fallback = RequestFallback.class)
public interface RequestClient extends RequestOperations {
}
