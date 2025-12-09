package com.slm.backend.controller;

import com.slm.backend.entity.Tag;
import com.slm.backend.repository.TagRepository;
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
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        return tagRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> createTag(@RequestBody Map<String, String> body) {
        String name = body.get("name");

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Name is required"));
        }

        if (tagRepository.existsByName(name)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Tag with this name already exists"));
        }

        String slug = generateSlug(name);

        Tag tag = Tag.builder()
            .name(name)
            .slug(slug)
            .build();

        tag = tagRepository.save(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTag(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Tag tag = tagRepository.findById(id)
            .orElse(null);

        if (tag == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.containsKey("name")) {
            tag.setName(body.get("name"));
            tag.setSlug(generateSlug(body.get("name")));
        }

        tag = tagRepository.save(tag);
        return ResponseEntity.ok(tag);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        if (!tagRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tagRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Tag deleted successfully"));
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
        while (tagRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}
