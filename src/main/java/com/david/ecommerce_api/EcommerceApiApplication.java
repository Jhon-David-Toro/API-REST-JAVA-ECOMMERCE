package com.david.ecommerce_api;

import com.david.ecommerce_api.auth.JwtProperties;
import com.david.ecommerce_api.config.CloudinaryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CloudinaryProperties.class, JwtProperties.class})
public class EcommerceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApiApplication.class, args);
	}

}
