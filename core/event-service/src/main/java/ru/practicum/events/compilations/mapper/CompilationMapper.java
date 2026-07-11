package ru.practicum.events.compilations.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.events.compilations.dto.CompilationDto;
import ru.practicum.events.compilations.dto.NewCompilationDto;
import ru.practicum.events.compilations.dto.UpdateCompilationRequest;
import ru.practicum.events.compilations.model.Compilation;
import ru.practicum.events.event.model.Event;
import ru.practicum.interaction.dto.event.EventShortDto;

import java.util.Set;

@Component
public class CompilationMapper {

    public CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> events) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setEvents(events);
        return compilationDto;
    }

    public Compilation fromNewCompilationDto(NewCompilationDto newCompilationDto,
                                             Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);
        return compilation;
    }

    public void applyPatch(UpdateCompilationRequest updateCompilationRequest,
                           Compilation compilation, Set<Event> events) {
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(events);
        }

    }
}
