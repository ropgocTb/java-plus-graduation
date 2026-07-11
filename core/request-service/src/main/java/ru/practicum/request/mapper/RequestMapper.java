package ru.practicum.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

@Component
public class RequestMapper {

    public ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent())
                .id(request.getId())
                .requester(request.getRequester())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> mapToListParticipationRequestDto(List<Request> requests) {
        return requests.stream()
                .map(this::mapToParticipationRequestDto)
                .toList();
    }
}
