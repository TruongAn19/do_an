package com.example.quanly.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.quanly.domain.dto.ProductCriteriaDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.repository.RacketRepository;
// import com.example.quanly.service.spectification.ProductSpec;

@Service
public class RacketService {
    @Autowired
    private RacketRepository racketRepository;

    public List<Racket> getAvailableRacketsByCourt(Long courtId) {
        return racketRepository.findByProductAndAvailableTrue(courtId);
    }

    // Phụ kiện
    public Page<Racket> getAllRacket(Pageable pageable) {
        return racketRepository.findAll(pageable);
    }

    public Racket handSaveRacket(Racket racket) {
        return this.racketRepository.save(racket);
    }
    public void deleteRacket(long racketId) {
        this.racketRepository.deleteById(racketId);
    }
    public Optional<Racket> getRacketById(long racketId) {
        return this.racketRepository.findById(racketId);
    }
    public Integer countRacket() {
        Integer total = this.racketRepository.countRackeQuantity();
        return total == null ? 0 : total;
    }


    public Page<Racket> getRackets(List<String> factories, List<String> prices, String sort, Pageable pageable) {
        Specification<Racket> spec = Specification.where(null);

        // Lọc theo hãng sản xuất
        if (factories != null && !factories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("factory").in(factories));
        }

        // Lọc theo mức giá
        if (prices != null && !prices.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                for (String price : prices) {
                    switch (price) {
                        case "duoi-500-nghin":
                            predicates.add(cb.lessThan(root.get("price"), 500000));
                            break;
                        case "500-nghin-1-trieu":
                            predicates.add(cb.between(root.get("price"), 500000, 1000000));
                            break;
                        case "1-5-trieu":
                            predicates.add(cb.between(root.get("price"), 1000000, 5000000));
                            break;
                        case "tren-5-trieu":
                            predicates.add(cb.greaterThan(root.get("price"), 5000000));
                            break;
                    }
                }
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        // Sắp xếp
        if (sort != null) {
            switch (sort) {
                case "gia-tang-dan":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by("price").ascending());
                    break;
                case "gia-giam-dan":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by("price").descending());
                    break;
            }
        }

        return racketRepository.findAll(spec, pageable);
    }

}
