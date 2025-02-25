package com.example.quanly.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.quanly.domain.Role;
import com.example.quanly.domain.User;
import com.example.quanly.repository.RoleRepository;
import com.example.quanly.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
}
