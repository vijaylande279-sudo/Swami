package com.swamisuite.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(RazorpayProperties.class)
public class RazorpayClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RazorpayClientConfig.class);
    private static final String LIVE_KEY_PREFIX = "rzp_live_";

    @Bean
    public RazorpayClient razorpayClient(RazorpayProperties properties, Environment environment) throws RazorpayException {
        // ADR 0002 §15.6 hard rule: fail to start if a live key is present outside prod.
        boolean isProd = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (StringUtils.hasText(properties.keyId()) && properties.keyId().startsWith(LIVE_KEY_PREFIX) && !isProd) {
            throw new IllegalStateException(
                    "A live Razorpay key (rzp_live_*) is configured but the 'prod' profile is not active. "
                            + "Refusing to start - see docs/adr/0002-payment-gateway-razorpay.md §15.6.");
        }

        if (!StringUtils.hasText(properties.keyId()) || !StringUtils.hasText(properties.keySecret())) {
            log.warn("No Razorpay credentials configured - checkout/webhook calls will fail until "
                    + "swamisuite.razorpay.key-id/key-secret are set. Fine for build/test; real sandbox "
                    + "testing needs test-mode keys.");
            return new RazorpayClient("rzp_test_unconfigured", "unconfigured");
        }

        return new RazorpayClient(properties.keyId(), properties.keySecret());
    }
}
