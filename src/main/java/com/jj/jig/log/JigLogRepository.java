package com.jj.jig.log;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JigLogRepository extends JpaRepository<JigLog, Long> {

    @EntityGraph(attributePaths = "user")
    List<JigLog> findByJigIdOrderByCreatedAtDesc(Long jigId);

    @EntityGraph(attributePaths = {"jig", "user"})
    @Query("SELECT l FROM JigLog l WHERE " +
           "(:fromDate IS NULL OR l.createdAt >= :fromDate) AND " +
           "(:toDate   IS NULL OR l.createdAt <= :toDate)   AND " +
           "(:actionType IS NULL OR l.actionType = :actionType) AND " +
           "(:username IS NULL OR l.user.username = :username) " +
           "ORDER BY l.createdAt DESC")
    List<JigLog> searchLogs(
            @Param("fromDate")   LocalDateTime fromDate,
            @Param("toDate")     LocalDateTime toDate,
            @Param("actionType") JigLogActionType actionType,
            @Param("username")   String username);
}
