package ru.practicum.interaction.contract.user;

import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.List;

public class PublicUserFallback implements PublicUserClient {
    @Override
    public List<UserShortDto> getUserFollowers(Long userId, int from, int size) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public List<UserShortDto> getUserFollowing(Long userId, int from, int size) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public UserDto getUserById(Long userId) {
        throw new RuntimeException("User service is unavailable");
    }

    @Override
    public boolean existsById(Long userId) {
        return false;
    }
}
