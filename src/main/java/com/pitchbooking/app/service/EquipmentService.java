package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.repository.EquipmentRepository;
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
public class EquipmentService {

    EquipmentRepository equipmentRepository;

    public List<Equipment> getAvailableEquipmentsByCourt(Long courtId) {
        return equipmentRepository.findByProductAndAvailableTrue(courtId);
    }

    public List<Equipment> getEquipmentsByProductId(Long productId) {
        return equipmentRepository.findByProductAndAvailableTrue(productId);
    }

    public Page<Equipment> getAllEquipment(Pageable pageable) {
        return equipmentRepository.findAll(pageable);
    }

    public Equipment handSaveEquipment(Equipment equipment) {
        return this.equipmentRepository.save(equipment);
    }

    public void deleteEquipment(long equipmentId) {
        this.equipmentRepository.deleteById(equipmentId);
    }

    public Optional<Equipment> getEquipmentById(long equipmentId) {
        return this.equipmentRepository.findById(equipmentId);
    }

    public Integer countEquipment() {
        Integer total = this.equipmentRepository.countRackeQuantity();
        return total == null ? 0 : total;
    }

    public Page<Equipment> getEquipments(List<String> factories, List<String> prices, String sort, Pageable pageable) {
        Specification<Equipment> spec = Specification.where(null);

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

        return equipmentRepository.findAll(spec, pageable);
    }

}
