package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.contract.user.AdminUserOperations;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Validated

@RequiredArgsConstructor
@Slf4j
public class AdminUserController implements AdminUserOperations {

    private final UserService userService;

    @Override
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("Creating user: email={}, name={}", newUserRequest.getEmail(), newUserRequest.getName());
        return userService.createUser(newUserRequest);
    }

    @Override
    public List<UserDto> getUsers(
            @RequestParam(name = "ids", required = false) List<@Positive Long> ids,
            @RequestParam(name = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", required = false, defaultValue = "10") @Positive Integer size
    ) {
        log.info("Getting users: ids={}, from={}, size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @Override
    public void deleteUser(@PathVariable @Positive Long userId) {
        log.info("Deleting user: id={}", userId);
        userService.deleteUser(userId);
    }
}