package ru.practicum.subscription.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.subscription.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByFollowerAndFollowed(Long userId, Long targetUserId);

    Optional<Subscription> findByFollowerAndFollowed(Long userId, Long targetUserId);

    @Query("""
            select s.follower
            from Subscription s
            where s.followed = :userId
            """)
    List<Long> findFollowers(Long userId, Pageable pageable);

    @Query("""
            select s.followed
            from Subscription s
            where s.follower = :userId
            """)
    List<Long> findFollowings(Long userId, Pageable pageable);
}
