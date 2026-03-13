package com.easycard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EasyCardApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyCardApplication.class, args);
    }
}
