package com.examly.springapp.controller;

import com.examly.springapp.model.User;
import com.examly.springapp.model.Role;
import com.examly.springapp.model.Category;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.repository.ExpenseRepository;
import com.examly.springapp.repository.CategoryRepository;
import com.examly.springapp.repository.BudgetRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import com.examly.springapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.persistence.Query;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> allUsers = userRepository.findAll();
        List<User> nonAdminUsers = allUsers.stream()
            .filter(user -> user.getRoles().stream()
                .noneMatch(role -> role.getName().equals(Role.ERole.ROLE_ADMIN)))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(nonAdminUsers);
    }
    
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long totalTransactions = expenseRepository.count();
        Double totalAmount = expenseRepository.sumAllExpenseAmounts();
        if (totalAmount == null) totalAmount = 0.0;
        
        // Get last 7 days registration data
        List<Integer> newRegistrations = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            long count = userRepository.countByCreatedAtBetween(dayStart, dayEnd);
            newRegistrations.add((int) count);
        }
        
        // Get top 5 categories by expense count
        Query topCategoriesQuery = entityManager.createNativeQuery(
            "SELECT c.name, COUNT(e.id) as expense_count " +
            "FROM categories c LEFT JOIN expenses e ON c.id = e.category_id " +
            "GROUP BY c.id, c.name ORDER BY expense_count DESC"
        );
        topCategoriesQuery.setMaxResults(5);
        List<Object[]> categoryResults = topCategoriesQuery.getResultList();
        
        List<Map<String, Object>> topCategories = new ArrayList<>();
        for (Object[] row : categoryResults) {
            Map<String, Object> category = new HashMap<>();
            category.put("name", row[0] != null ? row[0].toString() : "Unknown");
            category.put("count", row[1] != null ? ((Number) row[1]).intValue() : 0);
            topCategories.add(category);
        }
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalTransactions", totalTransactions);
        stats.put("totalAmount", totalAmount);
        stats.put("newRegistrations", newRegistrations);
        stats.put("topCategories", topCategories);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
    
    @PutMapping("/users/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> suspendUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setIsActive(false);
        userRepository.save(user);
        return ResponseEntity.ok("User suspended successfully");
    }
    
    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setIsActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("User activated successfully");
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            user.setIsActive(false);
            userRepository.save(user);
            
            return ResponseEntity.ok("User deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to deactivate user: " + e.getMessage());
        }
    }
    
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("username", user.getUsername());
        userDetails.put("email", user.getEmail());
        userDetails.put("firstName", user.getFirstName());
        userDetails.put("lastName", user.getLastName());
        userDetails.put("currency", user.getCurrency());
        userDetails.put("isActive", user.getIsActive());
        userDetails.put("createdAt", user.getCreatedAt());
        userDetails.put("expenseCount", expenseRepository.countByUserId(id));
        userDetails.put("budgetCount", budgetRepository.countByUserId(id));
        
        return ResponseEntity.ok(userDetails);
    }
    
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        
        if (updates.containsKey("firstName")) user.setFirstName(updates.get("firstName"));
        if (updates.containsKey("lastName")) user.setLastName(updates.get("lastName"));
        if (updates.containsKey("email")) user.setEmail(updates.get("email"));
        if (updates.containsKey("currency")) user.setCurrency(updates.get("currency"));
        
        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully");
    }
    
    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }
        
        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("Password reset successfully");
    }
    
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> request, Authentication authentication) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Category name is required");
        }
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User adminUser = userRepository.findById(userPrincipal.getId()).orElse(null);
            
            if (adminUser == null) {
                return ResponseEntity.badRequest().body("Admin user not found");
            }
            
            Category category = new Category();
            category.setName(name.trim());
            category.setUser(adminUser);
            categoryRepository.save(category);
            return ResponseEntity.ok("Category created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create category: " + e.getMessage());
        }
    }
    
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();
        
        String name = request.get("name");
        if (name != null && !name.trim().isEmpty()) {
            category.setName(name.trim());
            categoryRepository.save(category);
        }
        return ResponseEntity.ok("Category updated successfully");
    }
    
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok("Category deleted successfully");
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            // Get spending by category
            Query categorySpendingQuery = entityManager.createNativeQuery(
                "SELECT c.name, COALESCE(SUM(e.amount), 0) as total " +
                "FROM categories c LEFT JOIN expenses e ON c.id = e.category_id " +
                "GROUP BY c.id, c.name ORDER BY total DESC"
            );
            List<Object[]> categoryResults = categorySpendingQuery.getResultList();
            
            List<Map<String, Object>> categorySpending = new ArrayList<>();
            for (Object[] row : categoryResults) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", row[0] != null ? row[0].toString() : "Unknown");
                item.put("value", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                categorySpending.add(item);
            }
            analytics.put("categorySpending", categorySpending);
            
        } catch (Exception e) {
            System.err.println("Category spending query failed: " + e.getMessage());
            e.printStackTrace();
            analytics.put("categorySpending", new ArrayList<>());
        }
        
        try {
            // Get recent transactions
            Query transactionQuery = entityManager.createNativeQuery(
                "SELECT e.amount, c.name, e.description, e.date, u.first_name, u.last_name " +
                "FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id " +
                "JOIN users u ON e.user_id = u.id " +
                "ORDER BY e.date DESC"
            );
            transactionQuery.setMaxResults(10);
            List<Object[]> transactionResults = transactionQuery.getResultList();
            
            List<Map<String, Object>> recentTransactions = new ArrayList<>();
            for (Object[] row : transactionResults) {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("amount", row[0] != null ? ((Number) row[0]).doubleValue() : 0.0);
                transaction.put("category", row[1] != null ? row[1].toString() : "Unknown");
                transaction.put("description", row[2] != null ? row[2].toString() : "");
                transaction.put("date", row[3]);
                String firstName = row[4] != null ? row[4].toString() : "";
                String lastName = row[5] != null ? row[5].toString() : "";
                transaction.put("user", (firstName + " " + lastName).trim());
                recentTransactions.add(transaction);
            }
            analytics.put("recentTransactions", recentTransactions);
            
        } catch (Exception e) {
            System.err.println("Recent transactions query failed: " + e.getMessage());
            e.printStackTrace();
            analytics.put("recentTransactions", new ArrayList<>());
        }
        
        try {
            // Monthly spending trend - use EXTRACT for better compatibility
            Query monthlyTrendQuery = entityManager.createNativeQuery(
                "SELECT EXTRACT(YEAR FROM e.date) as year, EXTRACT(MONTH FROM e.date) as month, SUM(e.amount) as total " +
                "FROM expenses e " +
                "WHERE e.date >= CURRENT_DATE - INTERVAL '6 months' " +
                "GROUP BY EXTRACT(YEAR FROM e.date), EXTRACT(MONTH FROM e.date) " +
                "ORDER BY year DESC, month DESC"
            );
            List<Object[]> monthlyResults = monthlyTrendQuery.getResultList();
            
            List<Map<String, Object>> monthlyTrend = new ArrayList<>();
            for (Object[] row : monthlyResults) {
                Map<String, Object> item = new HashMap<>();
                int year = row[0] != null ? ((Number) row[0]).intValue() : 2024;
                int month = row[1] != null ? ((Number) row[1]).intValue() : 1;
                String monthStr = year + "-" + String.format("%02d", month);
                item.put("month", monthStr);
                item.put("amount", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                monthlyTrend.add(item);
            }
            analytics.put("monthlyTrend", monthlyTrend);
            
        } catch (Exception e) {
            System.err.println("Monthly trend query failed: " + e.getMessage());
            e.printStackTrace();
            analytics.put("monthlyTrend", new ArrayList<>());
        }
        
        try {
            // Top spending users
            Query topUsersQuery = entityManager.createNativeQuery(
                "SELECT u.first_name, u.last_name, u.email, COALESCE(SUM(e.amount), 0) as total, COUNT(e.id) as count " +
                "FROM users u " +
                "LEFT JOIN expenses e ON u.id = e.user_id " +
                "WHERE u.is_active = true " +
                "GROUP BY u.id, u.first_name, u.last_name, u.email " +
                "ORDER BY total DESC"
            );
            topUsersQuery.setMaxResults(5);
            List<Object[]> topUserResults = topUsersQuery.getResultList();
            
            List<Map<String, Object>> topUsers = new ArrayList<>();
            for (Object[] row : topUserResults) {
                Map<String, Object> user = new HashMap<>();
                String firstName = row[0] != null ? row[0].toString() : "";
                String lastName = row[1] != null ? row[1].toString() : "";
                user.put("name", (firstName + " " + lastName).trim());
                user.put("email", row[2] != null ? row[2].toString() : "");
                user.put("totalSpent", row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
                user.put("transactionCount", row[4] != null ? ((Number) row[4]).longValue() : 0L);
                topUsers.add(user);
            }
            analytics.put("topUsers", topUsers);
            
        } catch (Exception e) {
            System.err.println("Top users query failed: " + e.getMessage());
            e.printStackTrace();
            analytics.put("topUsers", new ArrayList<>());
        }
        
        try {
            // Budget vs actual analysis
            Query budgetAnalysisQuery = entityManager.createNativeQuery(
                "SELECT c.name, " +
                "COALESCE(SUM(b.allocated_amount), 0) as budgeted, " +
                "COALESCE(SUM(e.amount), 0) as actual " +
                "FROM categories c " +
                "LEFT JOIN budgets b ON c.id = b.category_id " +
                "LEFT JOIN expenses e ON c.id = e.category_id " +
                "GROUP BY c.id, c.name"
            );
            List<Object[]> budgetResults = budgetAnalysisQuery.getResultList();
            
            List<Map<String, Object>> budgetAnalysis = new ArrayList<>();
            for (Object[] row : budgetResults) {
                Map<String, Object> item = new HashMap<>();
                item.put("category", row[0] != null ? row[0].toString() : "Unknown");
                double budgeted = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                double actual = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                item.put("budgeted", budgeted);
                item.put("actual", actual);
                item.put("variance", actual - budgeted);
                item.put("percentUsed", budgeted > 0 ? (actual / budgeted) * 100 : 0);
                budgetAnalysis.add(item);
            }
            analytics.put("budgetAnalysis", budgetAnalysis);
            
        } catch (Exception e) {
            System.err.println("Budget analysis query failed: " + e.getMessage());
            e.printStackTrace();
            analytics.put("budgetAnalysis", new ArrayList<>());
        }
        
        // Ensure all required fields exist
        if (!analytics.containsKey("categorySpending")) analytics.put("categorySpending", new ArrayList<>());
        if (!analytics.containsKey("recentTransactions")) analytics.put("recentTransactions", new ArrayList<>());
        if (!analytics.containsKey("monthlyTrend")) analytics.put("monthlyTrend", new ArrayList<>());
        if (!analytics.containsKey("topUsers")) analytics.put("topUsers", new ArrayList<>());
        if (!analytics.containsKey("budgetAnalysis")) analytics.put("budgetAnalysis", new ArrayList<>());
        
        System.out.println("Analytics response: " + analytics);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/debug")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDebugInfo() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("userCount", userRepository.count());
        debug.put("expenseCount", expenseRepository.count());
        debug.put("categoryCount", categoryRepository.count());
        debug.put("budgetCount", budgetRepository.count());
        
        List<User> allUsers = userRepository.findAll();
        debug.put("users", allUsers.stream().map(u -> u.getEmail()).collect(java.util.stream.Collectors.toList()));
        
        return ResponseEntity.ok(debug);
    }
    
    @PostMapping("/seed-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> seedTestData() {
        try {
            // Create test categories if they don't exist
            String[] categoryNames = {"Food & Dining", "Transportation", "Shopping", "Entertainment", "Bills & Utilities"};
            User adminUser = userRepository.findById(1L).orElse(null);
            
            if (adminUser == null) {
                return ResponseEntity.badRequest().body("Admin user not found");
            }
            
            for (String catName : categoryNames) {
                if (categoryRepository.findAll().stream().noneMatch(c -> c.getName().equals(catName))) {
                    Category category = new Category();
                    category.setName(catName);
                    category.setUser(adminUser);
                    categoryRepository.save(category);
                }
            }
            
            return ResponseEntity.ok("Test data seeded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to seed data: " + e.getMessage());
        }
    }
}