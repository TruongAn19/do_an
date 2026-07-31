package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.exception.BusinessConflictException;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.RacketRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

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
        validateRacket(racket);
        return this.racketRepository.save(racket);
    }

    @Transactional
    public Racket updateRacket(long racketId, Racket updates) {
        validateRacket(updates);
        Racket existing = racketRepository.findByIdForUpdate(racketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + racketId));
        List<RacketStockByDate> futureStocks = racketStockByDateRepository
                .findFutureByRacketIdForUpdate(existing.getId(), LocalDate.now());
        for (RacketStockByDate stock : futureStocks) {
            int committed = stock.getReservedStock() + stock.getRentalStock();
            if (updates.getQuantity() < committed) {
                throw new BusinessConflictException(
                        "Không thể giảm tồn kho xuống " + updates.getQuantity()
                                + " vì ngày " + stock.getDate() + " đã có " + committed + " vợt được giữ/thuê.");
            }
            stock.setTotalStock(updates.getQuantity());
            stock.setAvailableStock(updates.getQuantity() - committed);
        }
        racketStockByDateRepository.saveAll(futureStocks);

        existing.setName(updates.getName());
        existing.setPrice(updates.getPrice());
        existing.setFactory(updates.getFactory());
        existing.setAvailable(updates.isAvailable());
        existing.setRentalPricePerDay(updates.getRentalPricePerDay());
        existing.setRentalPricePerPlay(updates.getRentalPricePerPlay());
        existing.setBookingStockQuantity(updates.getBookingStockQuantity());
        existing.setQuantity(updates.getQuantity());
        existing.setStatus(updates.getStatus() != null ? updates.getStatus() : existing.getStatus());
        existing.setProduct(updates.getProduct());
        if (updates.getImage() != null && !updates.getImage().isBlank()) {
            existing.setImage(updates.getImage());
        }
        return racketRepository.save(existing);
    }

    private void validateRacket(Racket racket) {
        if (racket.getName() == null || racket.getName().isBlank()) {
            throw new IllegalArgumentException("Tên vợt không được để trống.");
        }
        if (racket.getPrice() < 0 || racket.getRentalPricePerDay() < 0
                || racket.getRentalPricePerPlay() < 0) {
            throw new IllegalArgumentException("Giá vợt và giá thuê không thể âm.");
        }
        if (racket.getQuantity() < 0 || racket.getBookingStockQuantity() < 0) {
            throw new IllegalArgumentException("Tồn kho vợt không thể âm.");
        }
        if (racket.getProduct() == null || racket.getProduct().getId() <= 0) {
            throw new IllegalArgumentException("Vợt phải thuộc một sân.");
        }
    }

    @Transactional
    public void deleteRacket(long racketId) {
        Racket racket = racketRepository.findById(racketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + racketId));
        racket.setAvailable(false);
        racket.setStatus("DELETED");
        racketRepository.save(racket);
    }

    public Optional<Racket> getRacketById(long racketId) {
        return this.racketRepository.findById(racketId);
    }

    public Optional<Racket> getActiveRacketById(long racketId) {
        return racketRepository.findById(racketId)
                .filter(racket -> racket.isAvailable() && !"DELETED".equals(racket.getStatus()));
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
