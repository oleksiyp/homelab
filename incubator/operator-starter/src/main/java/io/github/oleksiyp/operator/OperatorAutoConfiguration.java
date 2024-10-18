package io.github.oleksiyp.operator;

import io.github.oleksiyp.operator.engine.OperatorEngine;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = OperatorEngine.class)
public class OperatorAutoConfiguration {
}
