package ru.practicum.interaction.contract.subscription;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface SubscriptionOperations {

    @PostMapping("/{userId}/subscriptions/{targetUserId}")
    void follow(@PathVariable @Positive Long userId, @PathVariable @Positive Long targetUserId);

    @DeleteMapping("/{userId}/subscriptions/{targetUserId}")
    void unfollow(@PathVariable @Positive Long userId, @PathVariable @Positive Long targetUserId);

    @GetMapping("/subscriptions/{userId}/followers")
    List<Long> getUserFollowers(@PathVariable @Positive Long userId,
                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                @RequestParam(defaultValue = "10") @Positive int size);

    @GetMapping("/subscriptions/{userId}/following")
    List<Long> getUserFollowing(@PathVariable @Positive Long userId,
                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                @RequestParam(defaultValue = "10") @Positive int size);
}
