package com.example.quanly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Bật tính năng Scheduled Tasks
public class QuanlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanlyApplication.class, args);
	}

}
