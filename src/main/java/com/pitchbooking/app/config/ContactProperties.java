package com.pitchbooking.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bound from {@code app.contact.*}; surfaced to users in cancel/refund responses
 * so they know who to contact about deposits.
 */
@Component
@ConfigurationProperties(prefix = "app.contact")
@Data
public class ContactProperties {
    private String hotline = "";
    private String email = "";
}
