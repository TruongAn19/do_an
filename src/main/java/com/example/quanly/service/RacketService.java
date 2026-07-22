package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.RacketStockByDateRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RacketService {

    RacketRepository racketRepository;
    RacketStockByDateRepository racketStockByDateRepository;

    public List<Racket> getAvailableRacketsByCourt(Long courtId) {
        return racketRepository.findByProductAndAvailableTrue(courtId);
    }

    public List<Racket> getRacketsByProductId(Long productId) {
        return racketRepository.findByProductAndAvailableTrue(productId);
    }

    public Page<Racket> getAllRacket(Pageable pageable) {
        Specification<Racket> visible = (root, query, cb) -> cb.or(
                cb.isNull(root.get("status")),
                cb.notEqual(root.get("status"), "RETIRED"));
        return racketRepository.findAll(visible, pageable);
    }

    public Racket handSaveRacket(Racket racket) {
        return this.racketRepository.save(racket);
    }

    @Transactional
    public Racket updateRacket(long racketId, Racket changes) {
        Racket existing = racketRepository.findByIdForUpdate(racketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + racketId));

        existing.setName(changes.getName());
        existing.setPrice(changes.getPrice());
        existing.setFactory(changes.getFactory());
        existing.setAvailable(changes.isAvailable());
        existing.setRentalPricePerDay(changes.getRentalPricePerDay());
        existing.setRentalPricePerPlay(changes.getRentalPricePerPlay());
        existing.setProduct(changes.getProduct());
        existing.setImage(changes.getImage());

        int requestedQuantity = changes.getQuantity();
        if (requestedQuantity < 1) {
            throw new IllegalArgumentException("Số lượng vợt phải lớn hơn hoặc bằng 1");
        }

        int currentTarget = existing.getTargetQuantity() != null
                ? existing.getTargetQuantity()
                : existing.getQuantity();
        int physicalQuantity = existing.getQuantity();
        int capacityDelta = requestedQuantity - currentTarget;

        existing.setTargetQuantity(requestedQuantity);
        existing.setBookingStockQuantity(Math.max(0,
                existing.getBookingStockQuantity() + capacityDelta));

        if (requestedQuantity >= physicalQuantity) {
            existing.setQuantity(requestedQuantity);
            existing.setPendingRetirementQuantity(0);
        } else {
            existing.setPendingRetirementQuantity(physicalQuantity - requestedQuantity);
        }

        applyCapacityToFutureStocks(existing.getId(), requestedQuantity);
        Racket saved = racketRepository.save(existing);
        tryFinalizeRetirement(saved.getId());
        return saved;
    }

    private void applyCapacityToFutureStocks(Long racketId, int capacity) {
        List<RacketStockByDate> stocks = racketStockByDateRepository
                .findByRacketIdAndDateGreaterThanEqual(racketId, LocalDate.now());
        for (RacketStockByDate stock : stocks) {
            int committed = stock.getReservedStock() + stock.getRentalStock();
            stock.setTotalStock(Math.max(capacity, committed));
            stock.setAvailableStock(Math.max(0, capacity - committed));
        }
    }

    @Transactional
    public void tryFinalizeRetirement(Long racketId) {
        Racket racket = racketRepository.findByIdForUpdate(racketId).orElse(null);
        if (racket == null || racket.getPendingRetirementQuantity() <= 0
                || racket.getTargetQuantity() == null) {
            return;
        }

        int target = racket.getTargetQuantity();
        boolean hasConflict = racketStockByDateRepository
                .findByRacketIdAndDateGreaterThanEqual(racketId, LocalDate.now())
                .stream()
                .anyMatch(stock -> stock.getReservedStock() + stock.getRentalStock() > target);
        if (!hasConflict) {
            racket.setQuantity(target);
            racket.setPendingRetirementQuantity(0);
            racketRepository.save(racket);
        }
    }

    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void finalizePendingRetirements() {
        racketRepository.findAll().stream()
                .filter(racket -> racket.getPendingRetirementQuantity() > 0)
                .forEach(racket -> tryFinalizeRetirement(racket.getId()));
    }

    public void deleteRacket(long racketId) {
        Racket racket = racketRepository.findById(racketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + racketId));
        racket.setAvailable(false);
        racket.setStatus("RETIRED");
        racketRepository.save(racket);
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
