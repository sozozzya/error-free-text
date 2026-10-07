package com.senla.errorfreetext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.speller")
public record SpellerProperties(
        String url,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
