package com.edutest.test_system.repositories;

import com.edutest.test_system.models.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    List<QuestionEntity> findByTestId(Long testId);
}
