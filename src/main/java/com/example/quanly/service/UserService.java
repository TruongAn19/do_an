package com.example.quanly.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quanly.domain.Role;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.RegisterDTO;
import com.example.quanly.repository.OrderRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RoleRepository;
import com.example.quanly.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UploadService uploadService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        
    }

    @PostConstruct
    public void initAdminUser() {
        if (userRepository.findByEmail("admin@gmail.com") == null) {
            User admin = new User();
            admin.setFullName("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPhone("0963931420");
            admin.setAddress("Hà Nội");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(getRoleByName("ADMIN"));
            userRepository.save(admin);
        }
    }

    public User handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public List<User> getAllUser() {
        return this.userRepository.findAll();
    }

    public User getUserById(long userId) {
        return this.userRepository.findById(userId);
    }

    public void deleteAUser(long id) {
        this.userRepository.deleteById(id);
    }

    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }

    public User registerDTOtoUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setFullName(registerDTO.getFirstName() + " " + registerDTO.getLastName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setAddress(null);
        user.setPhone(registerDTO.getPhone());
        return user;
    }

    public boolean checkEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public long countUser() {
        return this.userRepository.count();
    }

    public long countByProduct() {
        long count = this.productRepository.countByProduct();
        return count;
    }

    public long countMainProduct() {
        return this.productRepository.countMainProduct();
    }

    public long countOrder() {
        return this.orderRepository.count();
    }

    public User updateToUser(long id) {
        return this.userRepository.findById(id);
    }
}
