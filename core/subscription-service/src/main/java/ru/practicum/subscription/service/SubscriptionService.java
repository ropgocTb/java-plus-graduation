package ru.practicum.subscription.service;

import java.util.List;

public interface SubscriptionService {

    void follow(Long userId, Long targetUserId);

    void unfollow(Long userId, Long targetUserId);

    List<Long> getUserFollowers(Long userId, int from, int size);

    List<Long> getUserFollowings(Long userId, int from, int size);
}
