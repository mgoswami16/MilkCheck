package com.milkcheck.repository;

import com.milkcheck.entity.BulkEnquiry;
import com.milkcheck.enums.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BulkEnquiryRepository extends JpaRepository<BulkEnquiry, Long> {

    @Query("""
           SELECT e FROM BulkEnquiry e
           WHERE (:status IS NULL OR e.status = :status)
             AND (:keyword IS NULL OR :keyword = ''
                  OR LOWER(e.businessName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(e.refNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
           ORDER BY e.createdAt DESC
           """)
    Page<BulkEnquiry> search(
            @Param("status") EnquiryStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);
}