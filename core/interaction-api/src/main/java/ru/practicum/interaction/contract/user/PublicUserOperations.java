package ru.practicum.interaction.contract.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.List;

public interface PublicUserOperations {

    @GetMapping("/{userId}/followers")
    List<UserShortDto> getUserFollowers(@PathVariable @Positive Long userId,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size);

    @GetMapping("/{userId}/following")
    List<UserShortDto> getUserFollowing(@PathVariable @Positive Long userId,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size);

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable @Positive Long userId);

    @GetMapping("/{userId}/exists")
    boolean existsById(@PathVariable @Positive Long userId);
}
