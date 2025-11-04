package com.microservices.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("test")
class PaymentApplicationTests {

	@Test
	void contextLoads() {
	}

}
