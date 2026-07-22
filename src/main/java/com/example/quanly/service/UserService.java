package com.example.quanly.service;

import com.example.quanly.domain.Role;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.RegisterDTO;
import com.example.quanly.domain.dto.UserResponseDTO;
import com.example.quanly.mapper.UserMapper;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RoleRepository;
import com.example.quanly.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    ProductRepository productRepository;
    UserMapper userMapper;

    @PostConstruct
    public void initAdminUser() {
        Role adminRole = ensureRoleExists("ADMIN", "Quản trị viên hệ thống");
        ensureRoleExists("USER", "Khách hàng");
        ensureRoleExists("STAFF", "Nhân viên");

        if (userRepository.findByEmail("admin@gmail.com") == null) {
            User admin = new User();
            admin.setFullName("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPhone("0963931420");
            admin.setAddress("Hà Nội");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(adminRole);
            userRepository.save(admin);
        }
    }

    private Role ensureRoleExists(String name, String description) {
        Role existing = roleRepository.findByName(name);
        if (existing != null) {
            return existing;
        }
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    public UserResponseDTO handleSaveUser(User user) {
        return userMapper.toDTO(this.userRepository.save(user));
    }

    public List<UserResponseDTO> getAllUser() {
        return this.userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<UserResponseDTO> getUsers(Pageable pageable) {
        return userRepository.findByEnabledTrue(pageable).map(userMapper::toDTO);
    }

    public UserResponseDTO getUserById(long userId) {
        return userMapper.toDTO(this.userRepository.findUserById(userId));
    }

    public void deleteAUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void changePassword(long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Role getRoleByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vai trò không được để trống");
        }
        Role role = this.roleRepository.findByName(name.trim().toUpperCase());
        if (role == null) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + name);
        }
        return role;
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
