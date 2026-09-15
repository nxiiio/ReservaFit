package cl.nxiiio.ReservaFit_Backend.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // Booking rules ("today", "now") follow Chilean time; tests swap in a fixed clock
    public static final ZoneId ZONE = ZoneId.of("America/Santiago");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }
}
