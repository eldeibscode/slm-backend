package com.slm.backend.controller;

import com.slm.backend.entity.Category;
import com.slm.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        String color = body.get("color");

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Name is required"));
        }

        if (categoryRepository.existsByName(name)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Category with this name already exists"));
        }

        String slug = generateSlug(name);

        Category category = Category.builder()
            .name(name)
            .slug(slug)
            .description(description)
            .color(color)
            .build();

        category = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Category category = categoryRepository.findById(id)
            .orElse(null);

        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.containsKey("name")) {
            category.setName(body.get("name"));
            category.setSlug(generateSlug(body.get("name")));
        }
        if (body.containsKey("description")) {
            category.setDescription(body.get("description"));
        }
        if (body.containsKey("color")) {
            category.setColor(body.get("color"));
        }

        category = categoryRepository.save(category);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }

    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");

        slug = slug.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("[\\s]+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");

        String baseSlug = slug;
        int counter = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}
