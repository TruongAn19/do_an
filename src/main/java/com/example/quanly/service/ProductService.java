package com.example.quanly.service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.SubCourtAvailableTime;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.domain.dto.ProductResponseDTO;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.mapper.ProductMapper;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductService {

    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubCourtRepository subCourtRepository;
    SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;
    BookingDetailRepository bookingDetailRepository;
    TemporaryBookingRepository temporaryBookingRepository;
    RacketRepository racketRepository;
    ProductMapper productMapper;

    @Value("${booking.hold.duration-minutes:10}")
    @lombok.experimental.NonFinal
    int holdDurationMinutes;

    // Sân đấu
    public Page<ProductResponseDTO> getAllProductClient(Pageable pageable) {
        return this.productRepository.findByStatusNot("DELETED", pageable).map(productMapper::toDTO);
    }

    public Page<ProductResponseDTO> getAllProductAdmin(Pageable pageable) {
        Specification<Product> visible = (root, query, cb) -> cb.or(
                cb.isNull(root.get("status")),
                cb.notEqual(root.get("status"), "DELETED"));
        return this.productRepository.findAll(visible, pageable).map(productMapper::toDTO);
    }

    public ProductResponseDTO getCourtById(Long id) {
        return productRepository.findById(id).map(productMapper::toDTO).orElse(null);
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

        return this.productRepository.findAll(combinedSpec, pageable).map(productMapper::toDTO);
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

        if (isNew) {
            // Liên kết product với toàn bộ khung giờ → populate bảng court_time
            List<AvailableTime> allTimes = timeRepository.findAll();
            product.setAvailableTimes(new HashSet<>(allTimes));
        }

        Product savedProduct = productRepository.save(product);

        if (isNew) {
            // Tạo sub-courts và subcourt_available_time chỉ khi tạo mới
            List<AvailableTime> allTimes = timeRepository.findAll();
            
            String[] names = null;
            if (product.getSubCourtNames() != null && !product.getSubCourtNames().trim().isEmpty()) {
                names = product.getSubCourtNames().split(",");
            }
            
            int actualQuantity = (int) savedProduct.getQuantity();
            if (names != null && names.length > actualQuantity) {
                actualQuantity = names.length;
                savedProduct.setQuantity(actualQuantity);
                savedProduct = productRepository.save(savedProduct);
            }

            for (int i = 1; i <= actualQuantity; i++) {
                SubCourt subCourt = new SubCourt();
                subCourt.setActive(true);
                if (names != null && i <= names.length) {
                    subCourt.setName(names[i - 1].trim());
                } else {
                    subCourt.setName("Sân " + i);
                }
                subCourt.setProduct(savedProduct);
                subCourt = subCourtRepository.save(subCourt);

                for (AvailableTime availableTime : allTimes) {
                    SubCourtAvailableTime sat = new SubCourtAvailableTime();
                    sat.setSubCourt(subCourt);
                    sat.setAvailableTime(availableTime);
                    subCourtAvailableTimeRepository.save(sat);
                }
            }
        }

        return productMapper.toDTO(savedProduct);
    }

    @Transactional
    public ProductResponseDTO updateProduct(long productId, Product changes) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + productId));

        existing.setName(changes.getName());
        existing.setDetailDesc(changes.getDetailDesc() != null
                ? changes.getDetailDesc().replace("\n", "<br>")
                : "");
        existing.setPrice(changes.getPrice());
        existing.setSale(changes.getSale());
        existing.setAddress(changes.getAddress());
        existing.setAddressDetail(changes.getAddressDetail());
        existing.setShortDesc(changes.getShortDesc());

        if (changes.getImage() != null && !changes.getImage().isBlank()) {
            existing.setImage(changes.getImage());
        }

        syncSubCourts(existing, changes.getQuantity());
        return productMapper.toDTO(productRepository.save(existing));
    }

    private void syncSubCourts(Product product, long requestedQuantity) {
        if (requestedQuantity < 1 || requestedQuantity > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Số lượng sân phải lớn hơn hoặc bằng 1");
        }

        int targetQuantity = (int) requestedQuantity;
        List<SubCourt> allCourts = new ArrayList<>(subCourtRepository.findByProductId(product.getId()));
        allCourts.sort(Comparator.comparing(SubCourt::getId));
        List<SubCourt> activeCourts = allCourts.stream()
                .filter(SubCourt::isActive)
                .collect(Collectors.toCollection(ArrayList::new));
        List<SubCourt> inactiveCourts = allCourts.stream()
                .filter(court -> !court.isActive())
                .collect(Collectors.toCollection(ArrayList::new));
        Set<String> usedNames = allCourts.stream()
                .map(SubCourt::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<AvailableTime> allTimes = timeRepository.findAll();
        while (activeCourts.size() < targetQuantity && !inactiveCourts.isEmpty()) {
            SubCourt court = inactiveCourts.remove(0);
            court.setActive(true);
            activeCourts.add(court);
        }

        while (activeCourts.size() < targetQuantity) {
            SubCourt court = new SubCourt();
            String previousName = allCourts.isEmpty() ? null : allCourts.get(allCourts.size() - 1).getName();
            String newName = nextCourtName(previousName, allCourts.size() + 1, usedNames);
            court.setName(newName);
            court.setActive(true);
            court.setProduct(product);
            court = subCourtRepository.save(court);
            allCourts.add(court);
            activeCourts.add(court);
            usedNames.add(newName);

            for (AvailableTime time : allTimes) {
                SubCourtAvailableTime availability = new SubCourtAvailableTime();
                availability.setSubCourt(court);
                availability.setAvailableTime(time);
                subCourtAvailableTimeRepository.save(availability);
            }
        }

        if (targetQuantity < activeCourts.size()) {
            List<SubCourt> courtsToDeactivate = new ArrayList<>(
                    activeCourts.subList(targetQuantity, activeCourts.size()));
            LocalDate today = LocalDate.now();
            LocalDateTime activeHoldCutoff = LocalDateTime.now().minusMinutes(holdDurationMinutes);
            boolean inUse = courtsToDeactivate.stream().anyMatch(court ->
                    bookingDetailRepository.existsActiveFromDateBySubCourt(court, today)
                            || temporaryBookingRepository
                                    .existsBySubCourtAndHoldStartTimeGreaterThanEqual(court, activeHoldCutoff));
            if (inUse) {
                throw new IllegalArgumentException(
                        "Không thể giảm số lượng vì một hoặc nhiều sân cần đóng có lịch đặt hiện tại, tương lai hoặc đang được giữ chỗ");
            }

            for (SubCourt court : courtsToDeactivate) {
                court.setActive(false);
            }
        }

        product.setQuantity(targetQuantity);
    }

    private String nextCourtName(String previousName, int position, Set<String> usedNames) {
        String candidate = incrementCourtName(previousName);
        if (candidate == null || usedNames.contains(candidate)) {
            candidate = "Sân " + position;
        }

        int suffix = position;
        while (usedNames.contains(candidate)) {
            candidate = "Sân " + suffix++;
        }
        return candidate;
    }

    private String incrementCourtName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        java.util.regex.Matcher numberMatcher = java.util.regex.Pattern
                .compile("^(.*?)(\\d+)$")
                .matcher(name.trim());
        if (numberMatcher.matches()) {
            long nextNumber = Long.parseLong(numberMatcher.group(2)) + 1;
            return numberMatcher.group(1) + nextNumber;
        }

        java.util.regex.Matcher letterMatcher = java.util.regex.Pattern
                .compile("^(.*?)([A-Za-z]+)$")
                .matcher(name.trim());
        if (letterMatcher.matches()) {
            return letterMatcher.group(1) + incrementLetters(letterMatcher.group(2));
        }
        return null;
    }

    private String incrementLetters(String letters) {
        char[] value = letters.toUpperCase(Locale.ROOT).toCharArray();
        int index = value.length - 1;
        while (index >= 0 && value[index] == 'Z') {
            value[index] = 'A';
            index--;
        }
        if (index < 0) {
            return "A" + new String(value);
        }
        value[index]++;
        return new String(value);
    }

    public ProductResponseDTO getProductByID(long productId) {
        return productRepository.findById(productId)
                .map(productMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    public Optional<ProductResponseDTO> fetchProductById(long productId) {
        return productRepository.findById(productId).map(productMapper::toDTO);
    }

    @Transactional
    public void deleteAllProduct(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        product.setStatus("DELETED");
        subCourtRepository.findByProductId(productId).forEach(court -> court.setActive(false));
        racketRepository.findByProductId(productId).forEach(racket -> {
            racket.setAvailable(false);
            racket.setStatus("RETIRED");
        });
        productRepository.save(product);
    }

    public List<AvailableTime> getAllTime() {
        return this.timeRepository.findAll();
    }

    public Page<ProductResponseDTO> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map(productMapper::toDTO);
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

        return this.productRepository.findAll(spec, pageable).map(productMapper::toDTO);
    }

    public List<SubCourt> getAllCourtsByProduct(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        List<SubCourt> allCourts = this.subCourtRepository.findByProductIdAndActiveTrue(productId);

        // Giữ lại SubCourt đầu tiên theo tên
        Map<String, SubCourt> distinctByName = new LinkedHashMap<>();
        for (SubCourt court : allCourts) {
            distinctByName.putIfAbsent(court.getName(), court);
        }

        return new ArrayList<>(distinctByName.values());
    }

}
