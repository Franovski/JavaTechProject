package com.csis231.api.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // ✅ GET all categories
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        Map<String, Object> response = new HashMap<>();

        if (categories.isEmpty()) {
            response.put("message", "No categories found");
            response.put("data", Collections.emptyList());
            response.put("count", 0);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("message", "Categories retrieved successfully");
        response.put("count", categories.size());
        response.put("data", categories);

        return ResponseEntity.ok(response);
    }

    // ✅ GET category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        Map<String, Object> response = new HashMap<>();

        if (categoryOpt.isPresent()) {
            response.put("message", "Category retrieved successfully");
            response.put("data", categoryOpt.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Category not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ✅ GET category by name
    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, Object>> getCategoryByName(@PathVariable String name) {
        Optional<Category> categoryOpt = categoryRepository.findByName(name);
        Map<String, Object> response = new HashMap<>();

        if (categoryOpt.isPresent()) {
            response.put("message", "Category retrieved successfully");
            response.put("data", categoryOpt.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Category with name '" + name + "' not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ✅ POST - create new category
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            response.put("error", "Category name is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check duplicate name
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            response.put("error", "Category with name '" + category.getName() + "' already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        try {
            Category saved = categoryRepository.save(category);
            response.put("message", "Category created successfully");
            response.put("data", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to create category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ PUT - update category by ID
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody Category updatedCategory) {
        Map<String, Object> response = new HashMap<>();

        Optional<Category> existingCategoryOpt = categoryRepository.findById(id);
        if (existingCategoryOpt.isEmpty()) {
            response.put("error", "Category not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Validate input
        if (updatedCategory.getName() == null || updatedCategory.getName().trim().isEmpty()) {
            response.put("error", "Category name is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check duplicate name
        Optional<Category> duplicate = categoryRepository.findByName(updatedCategory.getName());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            response.put("error", "Another category with the same name already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        try {
            Category category = existingCategoryOpt.get();
            category.setName(updatedCategory.getName());
            // Add more fields if your Category has others

            Category saved = categoryRepository.save(category);

            response.put("message", "Category updated successfully");
            response.put("data", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to update category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ DELETE - delete category by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        if (!categoryRepository.existsById(id)) {
            response.put("error", "Category not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            categoryRepository.deleteById(id);
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to delete category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
