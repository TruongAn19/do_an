package com.example.quanly.service.spectification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Product_;

public class ProductSpec {

    public static Specification<Product> addressIsNullOrEmpty() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("address")),
                criteriaBuilder.equal(root.get("address"), ""));
    }

    public static Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }

    // case 1
    public static Specification<Product> minPrice(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.ge(root.get(Product_.PRICE), price);
    }

    // case 2
    public static Specification<Product> maxPrice(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.le(root.get(Product_.PRICE), price);
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
                criteriaBuilder.gt(root.get(Product_.PRICE), min),
                criteriaBuilder.le(root.get(Product_.PRICE), max));
    }

    // case6
    public static Specification<Product> matchMultiplePrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get(Product_.PRICE), min, max);
    }
}
