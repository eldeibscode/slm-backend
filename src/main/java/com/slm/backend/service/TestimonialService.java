package com.slm.backend.service;

import com.slm.backend.dto.CreateTestimonialRequest;
import com.slm.backend.dto.TestimonialDto;
import com.slm.backend.dto.UpdateTestimonialRequest;
import com.slm.backend.entity.Testimonial;
import com.slm.backend.repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Transactional(readOnly = true)
    public List<TestimonialDto> getAllTestimonials(String status) {
        List<Testimonial> testimonials;

        if (status != null && !status.isEmpty() && !status.equals("all")) {
            Testimonial.Status testimonialStatus = Testimonial.Status.valueOf(status.toUpperCase());
            testimonials = testimonialRepository.findByStatus(
                testimonialStatus,
                Sort.by(Sort.Direction.ASC, "displayOrder")
            );
        } else {
            testimonials = testimonialRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
        }

        return testimonials.stream()
                .map(TestimonialDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestimonialDto getTestimonialById(Long id) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Testimonial not found with id: " + id));
        return TestimonialDto.fromEntity(testimonial);
    }

    @Transactional
    public TestimonialDto createTestimonial(CreateTestimonialRequest request) {
        Testimonial testimonial = Testimonial.builder()
                .quote(request.getQuote())
                .author(request.getAuthor())
                .title(request.getTitle())
                .company(request.getCompany())
                .rating(request.getRating() != null ? request.getRating() : 5)
                .status(parseStatus(request.getStatus()))
                .displayOrder(request.getOrder() != null ? request.getOrder() : 0)
                .avatarUrl(request.getAvatarUrl())
                .build();

        Testimonial saved = testimonialRepository.save(testimonial);
        return TestimonialDto.fromEntity(saved);
    }

    @Transactional
    public TestimonialDto updateTestimonial(Long id, UpdateTestimonialRequest request) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Testimonial not found with id: " + id));

        if (request.getQuote() != null) {
            testimonial.setQuote(request.getQuote());
        }
        if (request.getAuthor() != null) {
            testimonial.setAuthor(request.getAuthor());
        }
        if (request.getTitle() != null) {
            testimonial.setTitle(request.getTitle());
        }
        if (request.getCompany() != null) {
            testimonial.setCompany(request.getCompany());
        }
        if (request.getRating() != null) {
            testimonial.setRating(request.getRating());
        }
        if (request.getStatus() != null) {
            testimonial.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getOrder() != null) {
            testimonial.setDisplayOrder(request.getOrder());
        }
        if (request.getAvatarUrl() != null) {
            testimonial.setAvatarUrl(request.getAvatarUrl());
        }

        Testimonial saved = testimonialRepository.save(testimonial);
        return TestimonialDto.fromEntity(saved);
    }

    @Transactional
    public void deleteTestimonial(Long id) {
        if (!testimonialRepository.existsById(id)) {
            throw new IllegalArgumentException("Testimonial not found with id: " + id);
        }
        testimonialRepository.deleteById(id);
    }

    private Testimonial.Status parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return Testimonial.Status.DRAFT;
        }
        try {
            return Testimonial.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Testimonial.Status.DRAFT;
        }
    }
}
