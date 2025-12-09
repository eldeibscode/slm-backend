package com.slm.backend.controller;

import com.slm.backend.dto.CreateTestimonialRequest;
import com.slm.backend.dto.TestimonialDto;
import com.slm.backend.dto.UpdateTestimonialRequest;
import com.slm.backend.service.TestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialService testimonialService;

    /**
     * Get all testimonials with optional status filter
     * Public endpoint
     */
    @GetMapping
    public ResponseEntity<List<TestimonialDto>> getAllTestimonials(
            @RequestParam(required = false, defaultValue = "published") String status
    ) {
        List<TestimonialDto> testimonials = testimonialService.getAllTestimonials(status);
        return ResponseEntity.ok(testimonials);
    }

    /**
     * Get testimonial by ID
     * Public endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestimonialDto> getTestimonialById(@PathVariable Long id) {
        TestimonialDto testimonial = testimonialService.getTestimonialById(id);
        return ResponseEntity.ok(testimonial);
    }

    /**
     * Create a new testimonial
     * Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTestimonial(
            @Valid @RequestBody CreateTestimonialRequest request
    ) {
        TestimonialDto testimonial = testimonialService.createTestimonial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Testimonial created successfully",
                "testimonial", testimonial
        ));
    }

    /**
     * Update a testimonial
     * Admin only
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateTestimonial(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTestimonialRequest request
    ) {
        TestimonialDto testimonial = testimonialService.updateTestimonial(id, request);
        return ResponseEntity.ok(Map.of(
                "message", "Testimonial updated successfully",
                "testimonial", testimonial
        ));
    }

    /**
     * Delete a testimonial
     * Admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTestimonial(@PathVariable Long id) {
        testimonialService.deleteTestimonial(id);
        return ResponseEntity.ok(Map.of("message", "Testimonial deleted successfully"));
    }
}
