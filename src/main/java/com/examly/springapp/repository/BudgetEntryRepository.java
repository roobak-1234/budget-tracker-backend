package com.examly.springapp.repository;

import com.examly.springapp.model.BudgetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetEntryRepository extends JpaRepository<BudgetEntry, Long> {
    List<BudgetEntry> findByCategory(String category);
    List<BudgetEntry> findByUserId(Long userId);
    List<BudgetEntry> findByCategoryAndUserId(String category, Long userId);
}