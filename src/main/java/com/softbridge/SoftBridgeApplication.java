package com.softbridge;
import java.lang.String;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SoftBridge — Software Requirements Portal
 * Spring Boot Backend Entry Point
 */
@SpringBootApplication
@EnableAsync
public class SoftBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftBridgeApplication.class, args);
    }
}
