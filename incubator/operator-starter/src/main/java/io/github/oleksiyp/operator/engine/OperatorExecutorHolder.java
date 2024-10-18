package io.github.oleksiyp.operator.engine;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@Getter
public class OperatorExecutorHolder {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
}
