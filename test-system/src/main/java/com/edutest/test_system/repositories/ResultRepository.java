package com.edutest.test_system.repositories;

import com.edutest.test_system.models.ResultEntity;
import com.edutest.test_system.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResultRepository extends JpaRepository<ResultEntity, Long> {
    List<ResultEntity> findByStudent(UserEntity student);
    List<ResultEntity> findByTestIdOrderByPercentageDesc(Long testId);
    
   
    List<ResultEntity> findByTestIdAndPercentage(Long testId, double percentage);
}
