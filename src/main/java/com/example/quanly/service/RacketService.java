package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.repository.RacketRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RacketService {

    RacketRepository racketRepository;

    public List<Racket> getAvailableRacketsByCourt(Long courtId) {
        return racketRepository.findByProductAndAvailableTrue(courtId);
    }

    public List<Racket> getRacketsByProductId(Long productId) {
        return racketRepository.findByProductAndAvailableTrue(productId);
    }

    /**
     * Danh sách vợt của sân (product) còn cho thuê theo booking — chỉ vợt có
     * bookingStockQuantity > 0. Dùng cho flow bundled rental khi đặt sân.
     */
    public List<Racket> getBookableRacketsByProduct(Long productId) {
        return racketRepository.findByProductAndAvailableTrue(productId).stream()
                .filter(r -> r.getBookingStockQuantity() > 0)
                .collect(java.util.stream.Collectors.toList());
    }

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

        spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), "DELETED"));

        if (factories != null && !factories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("factory").in(factories));
        }

        if (prices != null && !prices.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                for (String price : prices) {
                    switch (price) {
                        case "duoi-50-nghin":
                            predicates.add(cb.lessThan(root.get("rentalPricePerDay"), 50000));
                            break;
                        case "50-nghin-100-nghin":
                            predicates.add(cb.between(root.get("rentalPricePerDay"), 50000, 100000));
                            break;
                        case "100-nghin-200-nghin":
                            predicates.add(cb.between(root.get("rentalPricePerDay"), 100000, 200000));
                            break;
                        case "tren-200-nghin":
                            predicates.add(cb.greaterThan(root.get("rentalPricePerDay"), 200000));
                            break;
                    }
                }
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        if (sort != null) {
            switch (sort) {
                case "gia-tang-dan":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by("rentalPricePerDay").ascending());
                    break;
                case "gia-giam-dan":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by("rentalPricePerDay").descending());
                    break;
            }
        }

        return racketRepository.findAll(spec, pageable);
    }

}
