package com.jj.jig.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    List<UserLog> findTop50ByOrderByCreatedAtDesc();
}
