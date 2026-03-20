package com.example.gateway_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.gateway.discovery.locator.enabled=false"
        })
class GatewayServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
