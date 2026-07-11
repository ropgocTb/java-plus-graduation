package ru.practicum.interaction.contract.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.dto.event.EventFullDto;

public interface PublicEventOperations {

    @GetMapping("/{id}/no_hit")
    EventFullDto getEventNoHit(@PathVariable Long id);

    @GetMapping("/{eventId}/initiator/{userId}/exists")
    boolean existsByIdAndInitiator(@PathVariable Long eventId, @PathVariable Long userId);
}
