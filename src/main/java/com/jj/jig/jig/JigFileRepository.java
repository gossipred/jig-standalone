package com.jj.jig.jig;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JigFileRepository extends JpaRepository<JigFile, Long> {

    long countByJigId(Long jigId);

    List<JigFile> findByJigIdOrderByUploadedAtDesc(Long jigId);
}
