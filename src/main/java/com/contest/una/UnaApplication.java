package com.contest.una;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UnaApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnaApplication.class, args);
	}

}
