package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {

    @Query("""
            SELECT s FROM Subscriptions s
            LEFT JOIN FETCH s.category
            LEFT JOIN FETCH s.paymentMethod
            LEFT JOIN FETCH s.subscriptionType
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

    @Query("""
            SELECT s FROM Subscriptions s
            LEFT JOIN FETCH s.category
            LEFT JOIN FETCH s.paymentMethod
            LEFT JOIN FETCH s.subscriptionType
            WHERE s.user.id = :userId
            """)
    Page<Subscriptions> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT s FROM Subscriptions s
            LEFT JOIN FETCH s.category
            LEFT JOIN FETCH s.paymentMethod
            LEFT JOIN FETCH s.subscriptionType
            WHERE s.id = :id AND s.user.id = :userId
            """)
    Optional<Subscriptions> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            SELECT s FROM Subscriptions s
            LEFT JOIN FETCH s.category
            LEFT JOIN FETCH s.paymentMethod
            LEFT JOIN FETCH s.subscriptionType
            WHERE s.user.id = :userId
            AND s.active = true
            """)
    List<Subscriptions> findActiveByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT s FROM Subscriptions s
            JOIN FETCH s.user
            LEFT JOIN FETCH s.category
            LEFT JOIN FETCH s.paymentMethod
            WHERE s.nextPaymentDate = :nextPaymentDate
            AND s.active = true
            AND s.notify = true
            """)
    List<Subscriptions> findByNextPaymentDateAndActiveAndNotify(@Param("nextPaymentDate") LocalDate nextPaymentDate);
}
