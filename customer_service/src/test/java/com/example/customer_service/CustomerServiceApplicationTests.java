package com.example.customer_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class CustomerServiceApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("context load test");
	}
	@Test
	void mainMethodTest() {
		assertDoesNotThrow(() -> CustomerServiceApplication.main(new String[]{}));
		System.out.println("main method test");
	}
}
