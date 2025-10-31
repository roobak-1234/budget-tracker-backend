package com.examly.springapp.repository;

import com.examly.springapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    java.util.Optional<Category> findByNameAndUserId(String name, Long userId);
}