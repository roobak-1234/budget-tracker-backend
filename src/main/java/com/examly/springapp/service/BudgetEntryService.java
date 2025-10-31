package com.examly.springapp.service;

import com.examly.springapp.model.BudgetEntry;
import com.examly.springapp.repository.BudgetEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; 

@Service 
public class BudgetEntryService { 

    @Autowired
    private BudgetEntryRepository budgetEntryRepository;

    public BudgetEntry addEntry(BudgetEntry entry) {
        return budgetEntryRepository.save(entry);
    }

    public List<BudgetEntry> getAllEntries() {
        return budgetEntryRepository.findAll();
    }

    public List<BudgetEntry> getEntriesByCategory(String category) {
        return budgetEntryRepository.findByCategory(category);
    }

    public BudgetEntry getEntryById(Long id) {
        
        return budgetEntryRepository.findById(id).orElse(null);
    }

    public BudgetEntry updateEntry(Long id, BudgetEntry newEntryData) {
       
        Optional<BudgetEntry> existingEntryOptional = budgetEntryRepository.findById(id);

        if (existingEntryOptional.isPresent()) {
            BudgetEntry existingEntry = existingEntryOptional.get();
           
            existingEntry.setAmount(newEntryData.getAmount());
            existingEntry.setAmounttype(newEntryData.getAmounttype());
            existingEntry.setCategory(newEntryData.getCategory());
            existingEntry.setDescription(newEntryData.getDescription());
            existingEntry.setDate(newEntryData.getDate());
            
            return budgetEntryRepository.save(existingEntry); 
        }
        return null;
    }

    public void deleteEntry(Long id) {
        
        if (budgetEntryRepository.existsById(id)) {
            budgetEntryRepository.deleteById(id);
        }
    }

    public boolean entryExists(Long id) {
        return budgetEntryRepository.existsById(id);
    }
}