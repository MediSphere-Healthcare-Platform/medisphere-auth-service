package com.medisphere.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MedisphereAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedisphereAuthServiceApplication.class, args);
	}

}
