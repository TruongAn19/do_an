package com.example.quanly.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.SubCourtAvailableTime;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.SubCourtAvailableTimeRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.spectification.ProductSpec;

import jakarta.servlet.http.HttpSession;
import jakarta.persistence.criteria.Predicate;

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
    public Page<Product> getAllProduct(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Product getCourtById(Long id) {
        return productRepository.findById(id).orElse(null);
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
        return this.subCourtRepository.findByProduct(product);
    }


    public void handlePlaceBooking(User user, HttpSession session,
                                   String receiverName, String receiverAddress, String receiverPhone,
                                   long productId, long timeId, long subCourtId, LocalDate bookingDate) {

        // 1. Kiểm tra người dùng
        user = userRepository.findById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        // 2. Kiểm tra ngày đặt
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            throw new IllegalArgumentException("Không thể đặt sân cho ngày trong quá khứ.");
        }

        // 3. Lấy thông tin sân, khung giờ
        Product product = productRepository.getById(productId);
        SubCourt subCourt = subCourtRepository.getById(subCourtId);
        AvailableTime time = timeRepository.getById(timeId);

        // 4. Nếu đặt cho ngày hôm nay, kiểm tra khung giờ
        if (bookingDate.equals(today)) {
            LocalTime currentTime = LocalTime.now();
            if (time.getTime().isBefore(currentTime)) {
                throw new IllegalArgumentException("Khung giờ đã qua, vui lòng chọn giờ khác.");
            }
        }

        // 5. Kiểm tra sân đã được đặt chưa
        Optional<BookingDetail> existingBooking = bookingDetailRepository
                .findBySubCourtAndAvailableTimeAndDate(subCourt, time, bookingDate);

        if (existingBooking.isPresent()) {
            throw new IllegalArgumentException("Sân này đã được đặt cho khung giờ này vào ngày "
                    + bookingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
        }

        // 6. Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setReceiverName(receiverName);
        booking.setReceiverAddress(receiverAddress);
        booking.setReceiverPhone(receiverPhone);
        booking.setAvailableTime(time);
        booking.setStatus("Đã đặt");

        // 7. Tính toán giá
        double pricePerItem = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        booking.setTotalPrice(pricePerItem);
        booking = bookingRepository.save(booking); // Lưu để có ID

        // 8. Tạo booking detail
        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setBooking(booking);
        bookingDetail.setProduct(product);
        bookingDetail.setPrice(pricePerItem);
        bookingDetail.setSubCourt(subCourt);
        bookingDetail.setDate(bookingDate);
        bookingDetail.setSale(product.getSale());
        bookingDetail.setAvailableTime(time);
        bookingDetailRepository.save(bookingDetail);
    }

}
