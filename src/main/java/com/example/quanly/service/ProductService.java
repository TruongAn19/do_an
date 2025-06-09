package com.example.quanly.service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.SubCourtAvailableTime;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.repository.*;
import com.example.quanly.service.spectification.ProductSpec;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductService {

    UserRepository userRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubCourtRepository subCourtRepository;
    SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;

    BookingDetailRepository bookingDetailRepository;

    BookingRepository bookingRepository;


    // Sân đấu
    public Page<Product> getAllProductClient(Pageable pageable) {
        return this.productRepository.findByStatusNot("DELETED", pageable);
    }

    public Page<Product> getAllProductAdmin(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Product getCourtById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public long getCourtProduct() {
        return productRepository.count();
    }

    public Page<Product> getAllProductWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
        if (productCriteriaDTO.getAddress() == null
                && productCriteriaDTO.getPrice() == null) {
            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getAddress() != null && productCriteriaDTO.getAddress().isPresent()) {
            List<String> addresses = productCriteriaDTO.getAddress().get()
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (!addresses.isEmpty()) {
                Specification<Product> addressSpec = (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    for (String addr : addresses) {
                        predicates.add(cb.like(cb.lower(root.get("address")), "%" + addr.toLowerCase() + "%"));
                    }
                    return cb.or(predicates.toArray(new Predicate[0]));
                };
                combinedSpec = combinedSpec.and(addressSpec);
            }
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        combinedSpec = combinedSpec.and(Specification.not(ProductSpec.addressIsNullOrEmpty()));

        return this.productRepository.findAll(combinedSpec, pageable);
    }


    // lọc giá
    public Specification<Product> buildPriceSpecification(List<String> price) {
        Specification<Product> combinedSpec = Specification.where(null); // disconjunction
        for (String p : price) {
            double min = 0;
            double max = 0;

            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "duoi-500-nghin":
                    min = 1;
                    max = 500000;
                    break;
                case "500-nghin-1-trieu":
                    min = 500000;
                    max = 1000000;
                    break;
                case "1-5-trieu":
                    min = 1000000;
                    max = 5000000;
                    break;
                case "tren-5-trieu":
                    min = 5000000;
                    max = 200000000;
                    break;
            }

            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpec.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        return combinedSpec;
    }

    // -------------------------------//

    public Product handSaveProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        // Tạo SubCourts theo quantity
        for (int i = 1; i <= savedProduct.getQuantity(); i++) {
            SubCourt subCourt = new SubCourt();
            subCourt.setName("Sân " + i);
            subCourt.setProduct(savedProduct);
            subCourt = subCourtRepository.save(subCourt);

            // Tạo các available times cho từng SubCourt
            List<AvailableTime> availableTimes = this.timeRepository.findAll();
            for (AvailableTime availableTime : availableTimes) {
                SubCourtAvailableTime subCourtAvailableTime = new SubCourtAvailableTime();
                subCourtAvailableTime.setSubCourt(subCourt);
                subCourtAvailableTime.setAvailableTime(availableTime);
                subCourtAvailableTimeRepository.save(subCourtAvailableTime);
            }
        }

        return savedProduct;
    }

    public Product getProductByID(long productId) {
        return this.productRepository.getById(productId);
    }

    public Optional<Product> fetchProductById(long productId) {
        return Optional.ofNullable(this.productRepository.getById(productId));
    }

    public void deleteAllProduct(long productId) {
        this.productRepository.deleteById(productId);
    }

    public List<AvailableTime> getAllTime() {
        return this.timeRepository.findAll();
    }

    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public List<SubCourt> getAllCourtsByProduct(Product product) {
        List<SubCourt> allCourts = this.subCourtRepository.findByProduct(product);

        // Giữ lại SubCourt đầu tiên theo tên
        Map<String, SubCourt> distinctByName = new LinkedHashMap<>();
        for (SubCourt court : allCourts) {
            distinctByName.putIfAbsent(court.getName(), court);
        }

        return new ArrayList<>(distinctByName.values());
    }


}
