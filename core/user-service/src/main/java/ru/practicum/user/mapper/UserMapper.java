package ru.practicum.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;

@Component
public class UserMapper {

    public User mapToUser(NewUserRequest newUserRequest) {
        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    public UserDto mapToResponseDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserDto> mapToListResponseDto(List<User> users) {
        return users.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public List<UserShortDto> mapToListUserShortDto(List<User> users) {
        return users.stream()
                .map(this::mapToUserShortDto)
                .toList();
    }
}
