package ru.practicum.main.compilations.dto;

import lombok.Data;
import ru.practicum.interaction.dto.event.EventShortDto;

import java.util.Set;

@Data
public class CompilationDto {

    private Long id;
    private Boolean pinned;
    private String title;
    private Set<EventShortDto> events;

}
