package com.examly.springapp.repository;

import com.examly.springapp.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e")
    Double sumAllExpenseAmounts();
    
    void deleteByUserId(Long userId);
    Long countByUserId(Long userId);
}