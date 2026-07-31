package com.example.quanly.service;

import com.example.quanly.domain.Role;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.RegisterDTO;
import com.example.quanly.domain.dto.UserResponseDTO;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.mapper.UserMapper;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RoleRepository;
import com.example.quanly.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService {

    @Value("${app.bootstrap.admin.email:}")
    @NonFinal
    String bootstrapAdminEmail;

    @Value("${app.bootstrap.admin.password:}")
    @NonFinal
    String bootstrapAdminPassword;

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    ProductRepository productRepository;
    UserMapper userMapper;

    @PostConstruct
    public void initAdminUser() {
        if (bootstrapAdminEmail.isBlank() || bootstrapAdminPassword.isBlank()) {
            return;
        }
        if (userRepository.findByEmail(bootstrapAdminEmail) == null) {
            User admin = new User();
            admin.setFullName("admin");
            admin.setEmail(bootstrapAdminEmail);
            admin.setPhone("0987654321");
            admin.setAddress("Hà Nội");
            admin.setPassword(passwordEncoder.encode(bootstrapAdminPassword));
            admin.setRole(roleRepository.findByName("ADMIN"));
            userRepository.save(admin);
        }
    }

    public UserResponseDTO handleSaveUser(User user) {
        return userMapper.toDTO(this.userRepository.save(user));
    }

    public List<UserResponseDTO> getAllUser() {
        return this.userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(long userId) {
        return userMapper.toDTO(this.userRepository.findUserById(userId));
    }

    public void deleteAUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user id=" + id));
        user.setActive(false);
        userRepository.save(user);
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

    public UserResponseDTO getUserDTOByEmail(String email) {
        return userMapper.toDTO(this.userRepository.findByEmail(email));
    }

    public long countUser() {
        return this.userRepository.count();
    }

    public long countMainProduct() {
        return this.productRepository.count();
    }

    public User updateToUser(long id) {
        return this.userRepository.findUserById(id);
    }

    public User findByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}
