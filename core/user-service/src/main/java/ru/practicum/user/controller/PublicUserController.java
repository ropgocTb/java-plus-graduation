package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.contract.user.PublicUserOperations;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated

@RequiredArgsConstructor
@Slf4j
public class PublicUserController implements PublicUserOperations {

    private final UserService userService;

    @Override
    public List<UserShortDto> getUserFollowers(@PathVariable @Positive Long userId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Getting user followers: userId={}, from={}, size={}", userId, from, size);
        return userService.getUserFollowers(userId, from, size);
    }

    @Override
    public List<UserShortDto> getUserFollowing(@PathVariable @Positive Long userId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Getting user following: userId={}, from={}, size={}", userId, from, size);
        return userService.getUserFollowing(userId, from, size);
    }

    @Override
    public UserDto getUserById(@PathVariable @Positive Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public boolean existsById(@PathVariable @Positive Long userId) {
        return userService.existsById(userId);
    }
}
