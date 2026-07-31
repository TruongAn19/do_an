package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.PitchType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.SubPitchAvailableTime;
import com.pitchbooking.app.domain.dto.ProductCriteriaDTO;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.mapper.ProductMapper;
import com.pitchbooking.app.repository.*;
import com.pitchbooking.app.service.spectification.ProductSpec;
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

    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubPitchRepository subPitchRepository;
    SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    ProductMapper productMapper;

    // Sân đấu
    public Page<ProductResponseDTO> getAllProductClient(Pageable pageable) {
        return this.productRepository.findByStatusNot("DELETED", pageable)
                .map(this::enrichAndMap);
    }

    public Page<ProductResponseDTO> getAllProductAdmin(Pageable pageable) {
        return this.productRepository.findAll(pageable).map(this::enrichAndMap);
    }

    public List<ProductResponseDTO> getAllProductOptions() {
        return productRepository.findAll().stream()
                .filter(product -> !"DELETED".equals(product.getStatus()))
                .map(this::enrichAndMap)
                .toList();
    }

    public ProductResponseDTO getCourtById(Long id) {
        return productRepository.findById(id).map(this::enrichAndMap).orElse(null);
    }

    /**
     * Approach C: each Product has exactly one PitchType, stored on its SubPitches.
     * Read the first sub-pitch's type and stash it on the (transient) Product field
     * so the mapper can carry it into the response DTO.
     */
    private ProductResponseDTO enrichAndMap(Product product) {
        List<SubPitch> subs = subPitchRepository.findByProductId(product.getId());
        if (!subs.isEmpty() && subs.get(0).getPitchType() != null) {
            product.setPitchType(subs.get(0).getPitchType());
        }
        return productMapper.toDTO(product);
    }

    public long getCourtProduct() {
        return productRepository.count();
    }

    public Page<ProductResponseDTO> getAllProductWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
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
                        String searchStr = "%" + addr.toLowerCase() + "%";
                        predicates.add(cb.or(
                            cb.like(cb.lower(root.get("address")), searchStr),
                            cb.like(cb.lower(root.get("addressDetail")), searchStr)
                        ));
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

        return this.productRepository.findAll(combinedSpec, pageable).map(this::enrichAndMap);
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

    public ProductResponseDTO handSaveProduct(Product product) {
        boolean isNew = product.getId() == 0;
        PitchType pitchType = product.getPitchType() != null ? product.getPitchType() : PitchType.FIVE_ASIDE;

        if (isNew) {
            // Liên kết product với toàn bộ khung giờ → populate bảng court_time
            List<AvailableTime> allTimes = timeRepository.findAll();
            product.setAvailableTimes(new HashSet<>(allTimes));
        }

        Product savedProduct = productRepository.save(product);

        if (isNew) {
            // Tạo sub-courts và subpitch_available_time chỉ khi tạo mới
            List<AvailableTime> allTimes = timeRepository.findAll();

            String[] names = null;
            if (product.getSubPitchNames() != null && !product.getSubPitchNames().trim().isEmpty()) {
                names = product.getSubPitchNames().split(",");
            }

            int actualQuantity = (int) savedProduct.getQuantity();
            if (names != null && names.length > actualQuantity) {
                actualQuantity = names.length;
                savedProduct.setQuantity(actualQuantity);
                savedProduct = productRepository.save(savedProduct);
            }

            for (int i = 1; i <= actualQuantity; i++) {
                SubPitch subPitch = new SubPitch();
                if (names != null && i <= names.length) {
                    subPitch.setName(names[i - 1].trim());
                } else {
                    subPitch.setName("Sân " + i);
                }
                subPitch.setProduct(savedProduct);
                subPitch.setPitchType(pitchType);
                subPitch = subPitchRepository.save(subPitch);

                for (AvailableTime availableTime : allTimes) {
                    SubPitchAvailableTime sat = new SubPitchAvailableTime();
                    sat.setSubPitch(subPitch);
                    sat.setAvailableTime(availableTime);
                    subPitchAvailableTimeRepository.save(sat);
                }
            }
        } else {
            // On edit, propagate pitch type to all existing sub-pitches
            // (Approach C: one Product = one PitchType — all sub-pitches share it).
            List<SubPitch> subs = subPitchRepository.findByProductId(savedProduct.getId());
            for (SubPitch sp : subs) {
                if (sp.getPitchType() != pitchType) {
                    sp.setPitchType(pitchType);
                }
            }
            subPitchRepository.saveAll(subs);
        }

        savedProduct.setPitchType(pitchType);
        return productMapper.toDTO(savedProduct);
    }

    public ProductResponseDTO getProductByID(long productId) {
        return productRepository.findById(productId)
                .map(this::enrichAndMap)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    public Product getRawProductById(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    public Optional<ProductResponseDTO> fetchProductById(long productId) {
        return productRepository.findById(productId).map(this::enrichAndMap);
    }

    public void deleteAllProduct(long productId) {
        this.productRepository.deleteById(productId);
    }

    public List<AvailableTime> getAllTime() {
        return this.timeRepository.findAll();
    }

    public Page<ProductResponseDTO> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map(this::enrichAndMap);
    }

    public Page<ProductResponseDTO> searchProducts(String search, String address, Double maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                    cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%"));
        }

        if (address != null && !address.trim().isEmpty()) {
            String searchAddr = "%" + address.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                    cb.or(
                        cb.like(cb.lower(root.get("address")), searchAddr),
                        cb.like(cb.lower(root.get("addressDetail")), searchAddr)
                    )
            );
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        spec = spec.and(Specification.not(ProductSpec.addressIsNullOrEmpty()));

        spec = spec.and((root, query, cb) -> 
            cb.or(
                cb.notEqual(root.get("status"), "DELETED"),
                cb.isNull(root.get("status"))
            )
        );

        return this.productRepository.findAll(spec, pageable).map(this::enrichAndMap);
    }

    public List<SubPitch> getAllCourtsByProduct(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        List<SubPitch> allCourts = this.subPitchRepository.findByProduct(product);

        // Giữ lại SubPitch đầu tiên theo tên
        Map<String, SubPitch> distinctByName = new LinkedHashMap<>();
        for (SubPitch court : allCourts) {
            distinctByName.putIfAbsent(court.getName(), court);
        }

        return new ArrayList<>(distinctByName.values());
    }

}
