package ru.practicum.subscription.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.contract.subscription.SubscriptionOperations;
import ru.practicum.subscription.service.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated

@RequiredArgsConstructor
@Slf4j
public class PrivateSubscriptionController implements SubscriptionOperations {

    private final SubscriptionService subscriptionService;

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable @Positive Long userId,
                       @PathVariable @Positive Long targetUserId) {
        log.info("Following user: userId={}, targetUserId={}", userId, targetUserId);
        subscriptionService.follow(userId, targetUserId);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable @Positive Long userId,
                         @PathVariable @Positive Long targetUserId) {
        log.info("Unfollowing user: userId={}, targetUserId={}", userId, targetUserId);
        subscriptionService.unfollow(userId, targetUserId);
    }

    @Override
    public List<Long> getUserFollowers(Long userId, int from, int size) {
        log.info("Getting user followers ids: userId={}, from={}, size={}", userId, from, size);
        return subscriptionService.getUserFollowers(userId, from, size);
    }

    @Override
    public List<Long> getUserFollowing(Long userId, int from, int size) {
        log.info("Getting user following ids: userId={}, from={}, size={}", userId, from, size);
        return subscriptionService.getUserFollowings(userId, from, size);
    }
}
