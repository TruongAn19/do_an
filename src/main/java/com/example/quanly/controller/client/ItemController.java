package com.example.quanly.controller.client;

import java.time.LocalTime;
import java.util.ArrayList;

// import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Cart;
import com.example.quanly.domain.CartDetail;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.Product_;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ItemController {

    // private final TimeRepository timeRepository;
    private final ProductService productService;

    public ItemController(ProductService productService, TimeRepository timeRepository) {
        this.productService = productService;
        // this.timeRepository = timeRepository;
    }

    @GetMapping("/main-products")
    public String getMainProductPage(Model model,
            ProductCriteriaDTO productCriteriaDTO,
            HttpServletRequest request) {
        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                // convert from String to int
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        // check sort price
        Pageable pageable = PageRequest.of(page - 1, 6);

        Optional<String> sortOpt = productCriteriaDTO.getSort();

        if (sortOpt != null && sortOpt.isPresent()) {
            String sort = sortOpt.get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).descending());
            }
        }

        Page<Product> mainProduct;
        if (sortOpt != null && sortOpt.isPresent()
                && (productCriteriaDTO.getAddress() != null || productCriteriaDTO.getPrice() != null)) {
            mainProduct = this.productService.getAllMainProductWithSpec(pageable, productCriteriaDTO);
        } else if (sortOpt != null && sortOpt.orElse("").isEmpty()) {
            mainProduct = this.productService.getAllMainProduct(pageable);
        } else {
            mainProduct = this.productService.getAllMainProduct(pageable);
        }

        List<Product> listMainProducts = mainProduct.getContent().size() > 0 ? mainProduct.getContent()
                : new ArrayList<Product>();

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            // remove page
            qs = qs.replace("page=" + page, "");
        }
        model.addAttribute("listMainProducts", listMainProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", mainProduct.getTotalPages());
        model.addAttribute("queryString", qs);
        return "client/product/main_product";
    }

    @GetMapping("/by-products")
    public String getByProductPage(Model model,
            ProductCriteriaDTO productCriteriaDTO,
            HttpServletRequest request) {
        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                // convert from String to int
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        // check sort price
        Pageable pageable = PageRequest.of(page - 1, 6);

        Optional<String> sortOpt = productCriteriaDTO.getSort();

        if (sortOpt != null && sortOpt.isPresent()) {
            String sort = sortOpt.get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).descending());
            }
        }

        Page<Product> byProduct;
        if (sortOpt != null && sortOpt.isPresent()
                && (productCriteriaDTO.getFactory() != null || productCriteriaDTO.getPrice() != null)) {
            byProduct = this.productService.getAllByProductWithSpec(pageable, productCriteriaDTO);
        } else if (sortOpt != null && sortOpt.orElse("").isEmpty()) {
            byProduct = this.productService.getAllByProduct(pageable);
        } else {
            byProduct = this.productService.getAllByProduct(pageable);
        }

        List<Product> listByProduct = byProduct.getContent().size() > 0 ? byProduct.getContent()
                : new ArrayList<Product>();

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            // remove page
            qs = qs.replace("page=" + page, "");
        }
        model.addAttribute("listByProduct", listByProduct);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", byProduct.getTotalPages());
        model.addAttribute("queryString", qs);
        return "client/product/by_product";
    }

    @GetMapping("/mainProduct/{productId}")
    public String getMainProducts(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        List<AvailableTime> availableTime = this.productService.getAllTime();
        double discountPrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        model.addAttribute("discountPrice", discountPrice);
        model.addAttribute("availableTime", availableTime); // Gán vào model
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "client/product/detailMainProduct";
    }

    @GetMapping("/byProduct/{productId}")
    public String getByProducts(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        double discountPrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        model.addAttribute("discountPrice", discountPrice);
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "client/product/detailByProduct";
    }

    @GetMapping("/booking/{productId}")
    public String getBookingPage(Model model,
            HttpServletRequest request,
            @PathVariable long productId) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Product product = this.productService.getProductByID(productId);
        System.out.println(product);
        List<AvailableTime> allTimes  = this.productService.getAllTime();
        double totalPrice = 0;

        // Lọc giờ chưa qua
        LocalTime now = LocalTime.now();
        List<AvailableTime> availableTime = allTimes.stream()
                .filter(t -> t.getTime().isAfter(now)) // Không cần parse
                .collect(Collectors.toList());

        double price = product.getPrice();
        long quantity = 1;
        double discount = product.getSale() / 100.0;

        totalPrice += (price * quantity) - (price * quantity * discount);

        model.addAttribute("product", product);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("availableTime", availableTime);
        return "client/booking/booking_page";
    }

    @PostMapping("/place-booking")
    public String handlePlaceOrderBooking(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("productId") long productId,
            @RequestParam("quantity") int quantity,
            @RequestParam("availableTimeId") long timeId) {

        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");

        User currentUser = new User();
        currentUser.setId(id);

        productService.handlePlaceBooking(currentUser, session,
                receiverName, receiverAddress, receiverPhone,
                id, productId, quantity, timeId);

        return "redirect:/thanks";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long productId = id;
        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, productId, session, 1);
        return "redirect:/HomePage";
    }

    @GetMapping("/cart")
    public String getCart(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            double price = cartDetail.getPrice();
            long quantity = cartDetail.getQuantity();
            double discount = cartDetail.getProduct().getSale() / 100.0;

            totalPrice += (price * quantity) - (price * quantity * discount);
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cart", cart);
        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailId = id;
        this.productService.handleRemoveCartDetail(cartDetailId, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            double price = cartDetail.getPrice();
            long quantity = cartDetail.getQuantity();
            double discount = cartDetail.getProduct().getSale() / 100.0;

            totalPrice += (price * quantity) - (price * quantity * discount);
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        return "client/cart/checkout";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckOutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone);

        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String getThankYouPage(Model model) {

        return "client/cart/thanks";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(
            @RequestParam("id") long id,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") long quantity,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, id, session, quantity);
        return "redirect:/byProduct/" + id;
    }
}
