package com.furniture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FurnitureFactoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(FurnitureFactoryApplication.class, args);
        System.out.println("""
        \n=========================================
        Мебельная фабрика - система запущена!
        Доступно по адресу: http://localhost:8080
        Пользователи:
        - admin / admin123 (Директор)
        - master / master123 (Мастер цеха)
        - keeper / keeper123 (Кладовщик)
        =========================================
        """);
    }
}
