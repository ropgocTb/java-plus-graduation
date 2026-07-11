package ru.practicum.interaction.contract.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", path = "/events", fallback = PublicEventFallback.class)
public interface PublicEventClient extends PublicEventOperations {
}
