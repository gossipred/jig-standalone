package com.jj.jig.jig;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JigRepository extends JpaRepository<Jig, Long> {

    Optional<Jig> findByJigNo(String jigNo);

    boolean existsByJigNo(String jigNo);

    boolean existsByJigNoAndIdNot(String jigNo, Long id);

    List<Jig> findByJigBaseNoStartingWith(String jigBaseNoPrefix);

    @Query("""
            select distinct j from Jig j
            left join j.createdBy createdBy
            where lower(j.jigNo) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(j.classification, '')) like lower(concat('%', :keyword, '%'))
               or lower(j.modelName) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(j.assemblyLine, '')) like lower(concat('%', :keyword, '%'))
               or lower(j.jigName) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(j.customer, '')) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(createdBy.username, '')) like lower(concat('%', :keyword, '%'))
            """)
    List<Jig> searchByListKeyword(@Param("keyword") String keyword);
}
