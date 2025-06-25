package com.odevo.hoa.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.odevo.hoa") // Explicitly define base package for JPA repositories
@EntityScan(basePackages = "com.odevo.hoa.common.entity")
public class HoaWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HoaWorkerApplication.class, args);
    }
}
