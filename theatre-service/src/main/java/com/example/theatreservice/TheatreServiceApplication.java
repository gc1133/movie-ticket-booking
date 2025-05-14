package com.example.theatreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
//@EnableEurekaClient
public class TheatreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheatreServiceApplication.class, args);
    }
}