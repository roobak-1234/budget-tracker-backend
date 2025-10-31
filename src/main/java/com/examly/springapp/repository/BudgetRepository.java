package com.examly.springapp.repository;

import com.examly.springapp.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    Long countByUserId(Long userId);
}