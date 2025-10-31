package com.examly.springapp.controller;

import com.examly.springapp.dto.ExpenseDto;
import com.examly.springapp.model.Category;
import com.examly.springapp.model.Expense;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.CategoryRepository;
import com.examly.springapp.repository.ExpenseRepository;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> createExpense(@Valid @RequestBody ExpenseDto expenseDto, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid user");
        }

        // Find or create category by name
        Category category = categoryRepository.findByNameAndUserId(expenseDto.getCategoryName(), user.getId())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(expenseDto.getCategoryName());
                    newCategory.setUser(user);
                    return categoryRepository.save(newCategory);
                });

        Expense expense = new Expense();
        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setDate(expenseDto.getDate());
        expense.setPaymentMethod(expenseDto.getPaymentMethod());
        expense.setReceiptUrl(expenseDto.getReceiptUrl());
        expense.setIsRecurring(expenseDto.getIsRecurring());
        expense.setUser(user);
        expense.setCategory(category);

        return ResponseEntity.ok(expenseRepository.save(expense));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<List<Expense>> getUserExpenses(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Expense> expenses = expenseRepository.findByUserId(userPrincipal.getId());
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDto expenseDto, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);
        Expense expense = expenseRepository.findById(id).orElse(null);

        if (expense == null || !expense.getUser().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body("Expense not found or access denied");
        }

        // Find or create category by name
        Category category = categoryRepository.findByNameAndUserId(expenseDto.getCategoryName(), user.getId())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(expenseDto.getCategoryName());
                    newCategory.setUser(user);
                    return categoryRepository.save(newCategory);
                });

        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setDate(expenseDto.getDate());
        expense.setPaymentMethod(expenseDto.getPaymentMethod());
        expense.setReceiptUrl(expenseDto.getReceiptUrl());
        expense.setIsRecurring(expenseDto.getIsRecurring());
        expense.setCategory(category);

        return ResponseEntity.ok(expenseRepository.save(expense));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Expense expense = expenseRepository.findById(id).orElse(null);

        if (expense == null || !expense.getUser().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body("Expense not found or access denied");
        }

        expenseRepository.delete(expense);
        return ResponseEntity.ok("Expense deleted successfully");
    }
}