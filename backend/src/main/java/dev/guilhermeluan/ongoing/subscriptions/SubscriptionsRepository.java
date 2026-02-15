package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {

    @Query("""
            SELECT s FROM Subscriptions s
            WHERE (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS string), '%'))
            AND (:active IS NULL OR s.active = :active)
            AND (:categoryId IS NULL OR s.category.id = :categoryId)
            AND (s.user.id = :userId)
            """)
    Page<Subscriptions> findWithFilters(
            @Param("name") String name,
            @Param("active") Boolean active,
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId,
            Pageable pageable);

    Page<Subscriptions> findAllByUserId(Long userId, Pageable pageable);

    Optional<Subscriptions> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT s FROM Subscriptions s
            WHERE s.user.id = :userId
            AND s.active = true
            """)
    List<Subscriptions> findActiveByUserId(@Param("userId") Long userId);

    List<Subscriptions> User(User user);
}
