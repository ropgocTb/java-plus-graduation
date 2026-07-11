package ru.practicum.interaction.contract.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", path = "/events")
public interface PublicEventClient extends PublicEventOperations {
}
