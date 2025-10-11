package com.ucassignments.securesoftdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SecuredApp {

	public static void main(String[] args) {
		SpringApplication.run(SecuredApp.class, args);
	}

}
