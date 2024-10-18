package io.github.oleksiyp.operator.engine.properties;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class ResourceProperties {
    private Duration resyncPeriod = Duration.ofMinutes(10L);
    private Duration minDebounce = Duration.ofSeconds(1L);
    private Duration maxDebounce = Duration.ofSeconds(5L);
    private Duration backoffMin = Duration.ofSeconds(1L);
    private Duration backoffMax = Duration.ofSeconds(25L);
    private double backoffFactor = 1.23;
}
