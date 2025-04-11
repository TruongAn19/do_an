package com.example.quanly.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Cart;
import com.example.quanly.domain.CartDetail;
import com.example.quanly.domain.Order;
import com.example.quanly.domain.OrderDetail;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.repository.CartDetailRepository;
import com.example.quanly.repository.CartRepository;
import com.example.quanly.repository.OrderDetailRepository;
import com.example.quanly.repository.OrderRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.spectification.ProductSpec;

import jakarta.servlet.http.HttpSession;

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

    public ProductService(ProductRepository productRepository,
            UserRepository userRepository,
            TimeRepository timeRepository,
            CartDetailRepository cartDetailRepository,
            CartRepository cartRepository, UserService userService,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.timeRepository = timeRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // Sân đấu
    public Page<Product> getAllMainProduct(Pageable pageable) {
        return this.productRepository.findAllMainProducts(pageable);
    }

    public Page<Product> getAllMainProductWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
        if (productCriteriaDTO.getAddress() == null
                && productCriteriaDTO.getPrice() == null) {
            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getAddress() != null && productCriteriaDTO.getAddress().isPresent()) {
            String rawAddress = String.join(" ", productCriteriaDTO.getAddress().get());
            Specification<Product> currentSpecs = ProductSpec.matchAddressContainsAny(rawAddress);
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        combinedSpec = combinedSpec.and(Specification.not(ProductSpec.addressIsNullOrEmpty()));

        return this.productRepository.findAll(combinedSpec, pageable);
    }

    // Phụ kiện
    public Page<Product> getAllByProduct(Pageable pageable) {
        return productRepository.findAllByProducts(pageable);
    }

    public Page<Product> getAllByProductWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
        if (productCriteriaDTO.getFactory() == null
                && productCriteriaDTO.getPrice() == null) {
            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
            Specification<Product> currentSpecs = ProductSpec.matchListFactory(productCriteriaDTO.getFactory().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        combinedSpec = combinedSpec.and(ProductSpec.addressIsNullOrEmpty());

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
        return this.productRepository.save(product);
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
                    orderDetail.setQuantity(cd.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

                // step 2: delete cart_detail and cart
                for (CartDetail cd : cartDetails) {
                    this.cartDetailRepository.deleteById(cd.getId());
                    System.out.println(cd.getId());
                }
                this.cartDetailRepository.flush();
                System.out.println(this.cartDetailRepository.findAll());
                System.out.println(cart.getId());
                this.cartRepository.deleteById(cart.getId());

                // step 3 : update session
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handlePlaceBooking(User user, HttpSession session,
            String receiverName, String receiverAddress, String receiverPhone,
            long id, long productId, int quantity, long timeId) {

        // Lấy user và product từ database
        user = userRepository.findById(id);
        Product product = productRepository.getById(productId);
        AvailableTime time = timeRepository.getById(timeId);

        // Tạo đơn hàng (Order)
        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName);
        order.setReceiverAddress(receiverAddress);
        order.setReceiverPhone(receiverPhone);
        order.setAvailableTime(time); // Nếu có
        order.setStatus("Đã đặt");

        // Tính tổng giá
        double pricePerItem = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        double total = pricePerItem * quantity;

        order.setTotalPrice(total);
        orderRepository.save(order);

        // Tạo chi tiết đơn hàng (OrderDetail)
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        orderDetail.setPrice(pricePerItem); // Giá sau giảm
        orderDetail.setQuantity(quantity);
        orderDetail.setAvailableTime(time);

        orderDetailRepository.save(orderDetail);
    }

}
