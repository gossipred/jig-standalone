package com.jj.jig.jig;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JigFileRepository extends JpaRepository<JigFile, Long> {

    long countByJigId(Long jigId);

    @Query("select coalesce(sum(f.fileSize), 0) from JigFile f where f.jig.id = :jigId")
    long sumFileSizeByJigId(@Param("jigId") Long jigId);

    List<JigFile> findByJigIdOrderByUploadedAtDesc(Long jigId);
}
