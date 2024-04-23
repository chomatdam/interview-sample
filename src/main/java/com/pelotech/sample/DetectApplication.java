package com.pelotech.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.*;

@ConfigurationPropertiesScan
@SpringBootApplication
public class DetectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DetectApplication.class, args);
	}

}
