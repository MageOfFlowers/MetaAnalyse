package com.meta.analyzer; // Thay đổi theo package của bạn

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("model")
@EnableJpaRepositories("repository")
@ComponentScan({"service", "api", "com.meta.analyzer"})
public class CardGameApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardGameApplication.class, args);
    }
}