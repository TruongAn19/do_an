package com.example.quanly.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.Cart;
import com.example.quanly.domain.CartDetail;
import com.example.quanly.domain.Order;
import com.example.quanly.domain.OrderDetail;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.SubCourtAvailableTime;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.CartDetailRepository;
import com.example.quanly.repository.CartRepository;
import com.example.quanly.repository.OrderDetailRepository;
import com.example.quanly.repository.OrderRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.SubCourtAvailableTimeRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.spectification.ProductSpec;

import jakarta.servlet.http.HttpSession;
import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    private final UserRepository userRepository;

    private final ProductRepository productRepository;
    private final TimeRepository timeRepository;
    private final CartDetailRepository cartDetailRepository;
    private final CartRepository cartRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SubCourtRepository subCourtRepository;
    private SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;

    private final BookingDetailRepository bookingDetailRepository;

    private final BookingRepository bookingRepository;

    public ProductService(ProductRepository productRepository,
            UserRepository userRepository,
            TimeRepository timeRepository,
            CartDetailRepository cartDetailRepository,
            CartRepository cartRepository, UserService userService,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            SubCourtRepository subCourtRepository,
            SubCourtAvailableTimeRepository subCourtAvailableTimeRepository,
            BookingDetailRepository bookingDetailRepository,
            BookingRepository bookingRepository) {
        this.productRepository = productRepository;
        this.timeRepository = timeRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.subCourtRepository = subCourtRepository;
        this.subCourtAvailableTimeRepository = subCourtAvailableTimeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.bookingRepository = bookingRepository;
    }

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
                // subCourtAvailableTime.setBooked(false);
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

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {

        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            // check user đã có Cart chưa ? nếu chưa -> tạo mới
            Cart cart = this.cartRepository.findByUser(user);

            if (cart == null) {
                // tạo mới cart
                Cart otherCart = new Cart();
                otherCart.setUser(user);
                otherCart.setSum(0);

                cart = this.cartRepository.save(otherCart);
            }

            // save cart_detail
            // tìm product by id

            Optional<Product> productOptional = this.productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product realProduct = productOptional.get();

                // check sản phẩm đã từng được thêm vào giỏ hàng trước đây chưa ?
                CartDetail oldDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);
                //
                if (oldDetail == null) {
                    CartDetail cd = new CartDetail();
                    cd.setCart(cart);
                    cd.setProduct(realProduct);
                    cd.setPrice(realProduct.getPrice());
                    cd.setQuantity(quantity);
                    this.cartDetailRepository.save(cd);

                    // update cart (sum);
                    int s = cart.getSum() + 1;
                    cart.setSum(s);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", s);
                } else {
                    oldDetail.setQuantity(oldDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldDetail);
                }

            }

        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleRemoveCartDetail(long id, HttpSession session) {
        Optional<CartDetail> optionalCartDetail = this.cartDetailRepository.findById(id);
        if (optionalCartDetail.isPresent()) {
            CartDetail cartDetail = optionalCartDetail.get();
            Cart currentCart = cartDetail.getCart();
            this.cartDetailRepository.deleteById(id);

            if (currentCart.getSum() >= 1) {
                int sum = currentCart.getSum() - 1;
                currentCart.setSum(sum);
                session.setAttribute("sum", sum);
                this.cartRepository.save(currentCart);
            } else {
                this.cartRepository.deleteById(currentCart.getId());
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    public void handlePlaceOrder(
            User user, HttpSession session,
            String receiverName, String receiverAddress, String receiverPhone) {

        // step 1: get cart by user
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();

            if (cartDetails != null) {

                // create order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double sum = 0;
                for (CartDetail cd : cartDetails) {
                    sum += cd.getPrice() - (cd.getPrice() * cd.getProduct().getSale() / 100);
                }
                order.setTotalPrice(sum);
                order = this.orderRepository.save(order);

                // create orderDetail

                for (CartDetail cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice(cd.getPrice());
                    // orderDetail.setQuantity(cd.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

                // step 2: delete cart_detail and cart
                for (CartDetail cd : cartDetails) {
                    cd.setCart(null);
                    this.cartDetailRepository.deleteById(cd.getId());
                    System.out.println(cd.getId());
                }
                this.cartDetailRepository.flush();

                cart.setSum(0);
                this.cartRepository.save(cart);
                // step 3 : update session
                session.setAttribute("sum", 0);
            }

        }
    }

    public void handlePlaceBooking(User user, HttpSession session,
            String receiverName, String receiverAddress, String receiverPhone,
            long productId, int quantity, long timeId, long subCourtId, LocalDate bookingDate) {

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
        booking.setBookingDate(bookingDate);
        booking.setStatus("Đã đặt");

        // 7. Tính toán giá
        double pricePerItem = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        booking.setTotalPrice(pricePerItem * quantity);
        booking = bookingRepository.save(booking); // Lưu để có ID

        // 8. Tạo booking detail
        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setBooking(booking);
        bookingDetail.setProduct(product);
        bookingDetail.setPrice(pricePerItem);
        bookingDetail.setQuantity(quantity);
        bookingDetail.setSubCourt(subCourt);
        bookingDetail.setDate(bookingDate);
        bookingDetail.setSale(product.getSale());
        bookingDetail.setAvailableTime(time);
        bookingDetailRepository.save(bookingDetail);
    }

}
