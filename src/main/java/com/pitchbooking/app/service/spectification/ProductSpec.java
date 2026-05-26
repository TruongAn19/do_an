package com.pitchbooking.app.service.spectification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import com.pitchbooking.app.domain.Product;

public class ProductSpec {

    public static Specification<Product> addressIsNullOrEmpty() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("address")),
                criteriaBuilder.equal(root.get("address"), ""));
    }

    public static Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    // case 1
    public static Specification<Product> minPrice(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.ge(root.get("price"), price);
    }

    // case 2
    public static Specification<Product> maxPrice(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.le(root.get("price"), price);
    }

    public static Specification<Product> matchAddressContainsAny(String rawAddress) {
        return (root, query, criteriaBuilder) -> {
            if (rawAddress == null || rawAddress.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // không lọc nếu không có input
            }

            String[] parts = rawAddress.toLowerCase().split(",");

            List<Predicate> predicates = new ArrayList<>();

            for (String part : parts) {
                part = part.trim();
                if (!part.isBlank()) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("address")),
                            "%" + part + "%"));
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }



    // case5
    public static Specification<Product> matchPrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.gt(root.get("price"), min),
                criteriaBuilder.le(root.get("price"), max));
    }

    // case6
    public static Specification<Product> matchMultiplePrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get("price"), min, max);
    }
}
