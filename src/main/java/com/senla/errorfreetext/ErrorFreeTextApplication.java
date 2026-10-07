package com.senla.errorfreetext;

import com.senla.errorfreetext.config.SpellerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SpellerProperties.class)
public class ErrorFreeTextApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErrorFreeTextApplication.class, args);
    }
}
