package com.example.quanly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)

public class QuanlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanlyApplication.class, args);
	}

}
