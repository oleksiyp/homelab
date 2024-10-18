package io.github.oleksiyp.operator.engine.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("operator")
@Getter
@Setter
public class OperatorProperties {
    private Map<String, ResourceProperties> resources = Map.of();
}
