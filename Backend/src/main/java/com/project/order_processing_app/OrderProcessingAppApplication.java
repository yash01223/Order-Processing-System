package com.project.order_processing_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderProcessingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderProcessingAppApplication.class, args);
	}
}
