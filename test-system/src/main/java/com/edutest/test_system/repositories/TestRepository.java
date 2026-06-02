package com.edutest.test_system.repositories;

import com.edutest.test_system.models.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    
    List<TestEntity> findByPublished(boolean published);
    
    List<TestEntity> findByAuthorEmail(String authorEmail);

  
    TestEntity findByTestCode(String testCode);

    
    @Query("SELECT t FROM TestEntity t WHERE t.published = true AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.targetAudience) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TestEntity> searchPublishedTests(@Param("keyword") String keyword);
}