package ru.practicum.subscription.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.contract.user.PublicUserClient;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.util.PaginationUtil;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PublicUserClient userClient;

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) throw new BadRequestException("User cannot follow himself");
        if (!userClient.existsById(userId)) throw new NotFoundException("User not found " + userId);
        if (!userClient.existsById(targetUserId)) throw new NotFoundException("User not found " + targetUserId);

        boolean alreadyExists = subscriptionRepository.existsByFollowerAndFollowed(userId, targetUserId);
        if (alreadyExists) throw new ConflictException("User already subscribed to this user");

        Subscription subscription = Subscription.builder()
                .follower(userId)
                .followed(targetUserId)
                .created(LocalDateTime.now())
                .build();
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) throw new BadRequestException("User cannot unfollow himself");

        Subscription subscription = subscriptionRepository.findByFollowerAndFollowed(userId, targetUserId)
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        subscriptionRepository.delete(subscription);
    }

    @Override
    public List<Long> getUserFollowers(Long userId, int from, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(from, size);
        return subscriptionRepository.findFollowers(userId, pageable);
    }

    @Override
    public List<Long> getUserFollowings(Long userId, int from, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(from, size);
        return subscriptionRepository.findFollowings(userId, pageable);
    }

}
