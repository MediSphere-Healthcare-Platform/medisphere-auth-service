package com.medisphere.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedisphereAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedisphereAuthServiceApplication.class, args);
	}

}
