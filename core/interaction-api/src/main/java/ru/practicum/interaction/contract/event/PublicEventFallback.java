package ru.practicum.interaction.contract.event;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.event.EventFullDto;

@Component
public class PublicEventFallback implements PublicEventClient {

    @Override
    public EventFullDto getEventNoHit(Long id) {
        throw new RuntimeException("Event service is unavailable");
    }

    @Override
    public boolean existsByIdAndInitiator(Long eventId, Long userId) {
        return false;
    }
}
