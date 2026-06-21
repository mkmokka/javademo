package com.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Collections;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        
        // Render Cloud-এর ডাইনামিক পোর্ট রিড করার লজিক
        String port = System.getenv("PORT");
        if (port == null || port.isEmpty()) {
            port = "8080"; // লোকাল হোস্টের জন্য ডিফল্ট ব্যাকআপ পোর্ট
        }
        
        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
        System.out.println("Chess Application Successfully Started on Port: " + port);
    }
}
