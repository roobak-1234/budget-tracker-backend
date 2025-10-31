package com.examly.springapp.controller;

import com.examly.springapp.model.Budget;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.BudgetRepository;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<List<Budget>> getAllBudgets(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Budget> budgets = budgetRepository.findByUserId(userPrincipal.getId());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return budgetRepository.findById(id)
                .filter(budget -> budget.getUser().getId().equals(userPrincipal.getId()))
                .map(budget -> ResponseEntity.ok().body(budget))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        budget.setUser(user);
        return ResponseEntity.ok(budgetRepository.save(budget));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, @RequestBody Budget budgetDetails, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return budgetRepository.findById(id)
                .filter(budget -> budget.getUser().getId().equals(userPrincipal.getId()))
                .map(budget -> {
                    budget.setName(budgetDetails.getName());
                    budget.setAllocatedAmount(budgetDetails.getAllocatedAmount());
                    budget.setStartDate(budgetDetails.getStartDate());
                    budget.setEndDate(budgetDetails.getEndDate());
                    budget.setBudgetType(budgetDetails.getBudgetType());
                    return ResponseEntity.ok(budgetRepository.save(budget));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return budgetRepository.findById(id)
                .filter(budget -> budget.getUser().getId().equals(userPrincipal.getId()))
                .map(budget -> {
                    budgetRepository.delete(budget);
                    return ResponseEntity.ok().build();
                }).orElse(ResponseEntity.notFound().build());
    }
}