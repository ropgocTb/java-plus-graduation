package ru.practicum.user.service;


import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

    List<UserShortDto> getUserFollowers(Long userId, int from, int size);

    List<UserShortDto> getUserFollowing(Long userId, int from, int size);

    UserDto getUserById(Long userId);

    boolean existsById(Long userId);
}
