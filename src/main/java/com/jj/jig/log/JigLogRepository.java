package com.jj.jig.log;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JigLogRepository extends JpaRepository<JigLog, Long> {

    @EntityGraph(attributePaths = "user")
    List<JigLog> findByJigIdOrderByCreatedAtDesc(Long jigId);
}
