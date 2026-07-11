package ru.practicum.interaction.contract.subscription;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionFallback implements SubscriptionClient {
    @Override
    public void follow(Long userId, Long targetUserId) {
        throw new RuntimeException("Subscription service is unavailable");
    }

    @Override
    public void unfollow(Long userId, Long targetUserId) {
        throw new RuntimeException("Subscription service is unavailable");
    }

    @Override
    public List<Long> getUserFollowers(Long userId, int from, int size) {
        throw new RuntimeException("Subscription service is unavailable");
    }

    @Override
    public List<Long> getUserFollowing(Long userId, int from, int size) {
        throw new RuntimeException("Subscription service is unavailable");
    }
}
