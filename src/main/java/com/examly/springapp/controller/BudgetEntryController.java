package com.examly.springapp.controller;

import com.examly.springapp.model.BudgetEntry;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.BudgetEntryRepository;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/budget")
public class BudgetEntryController {

    @Autowired
    private BudgetEntryRepository budgetEntryRepository;
    
    @Autowired
    private UserRepository userRepository;


    @PostMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> addBudgetEntry(@RequestBody BudgetEntry entry, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        entry.setUser(user);
        BudgetEntry newEntry = budgetEntryRepository.save(entry);
        return new ResponseEntity<>(newEntry, HttpStatus.OK);
    }

    
    @GetMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<List<BudgetEntry>> getAllEntries(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<BudgetEntry> entries = budgetEntryRepository.findByUserId(userPrincipal.getId());
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }
    
 
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<List<BudgetEntry>> getEntriesByCategory(@PathVariable String category, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<BudgetEntry> entries = budgetEntryRepository.findByCategoryAndUserId(category, userPrincipal.getId());
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> getEntryById(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        BudgetEntry entry = budgetEntryRepository.findById(id).orElse(null);
        
        if (entry == null || !entry.getUser().getId().equals(userPrincipal.getId())) {
            return new ResponseEntity<>("Entry not found or access denied", HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(entry, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> updateEntry(@PathVariable Long id, @RequestBody BudgetEntry entry, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        BudgetEntry existingEntry = budgetEntryRepository.findById(id).orElse(null);
        
        if (existingEntry == null || !existingEntry.getUser().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body("Entry not found or access denied");
        }
        
        existingEntry.setAmount(entry.getAmount());
        existingEntry.setAmounttype(entry.getAmounttype());
        existingEntry.setCategory(entry.getCategory());
        existingEntry.setDescription(entry.getDescription());
        existingEntry.setDate(entry.getDate());
        
        BudgetEntry updatedEntry = budgetEntryRepository.save(existingEntry);
        return new ResponseEntity<>(updatedEntry, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        BudgetEntry entry = budgetEntryRepository.findById(id).orElse(null);
        
        if (entry == null || !entry.getUser().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body("Entry not found or access denied");
        }
        
        budgetEntryRepository.delete(entry);
        return new ResponseEntity<>("Entry deleted successfully", HttpStatus.NO_CONTENT);
    }
}