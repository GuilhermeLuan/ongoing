package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {

    @Query("""
            SELECT s FROM Subscriptions s
            WHERE (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS string), '%'))
            AND (:active IS NULL OR s.active = :active)
            AND (:categoryId IS NULL OR s.category.id = :categoryId)
            """)
    Page<Subscriptions> findWithFilters(
            @Param("name") String name,
            @Param("active") Boolean active,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
}
