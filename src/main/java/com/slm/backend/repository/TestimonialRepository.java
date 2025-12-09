package com.slm.backend.repository;

import com.slm.backend.entity.Testimonial;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    List<Testimonial> findByStatus(Testimonial.Status status, Sort sort);

    List<Testimonial> findAllByOrderByDisplayOrderAsc();

    List<Testimonial> findByStatusOrderByDisplayOrderAsc(Testimonial.Status status);
}
