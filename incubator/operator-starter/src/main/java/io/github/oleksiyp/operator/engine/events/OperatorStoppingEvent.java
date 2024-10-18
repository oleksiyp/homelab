package io.github.oleksiyp.operator.engine.events;

import io.github.oleksiyp.operator.engine.OperatorEngine;
import org.springframework.context.ApplicationEvent;

public class OperatorStoppingEvent extends ApplicationEvent {
    public OperatorStoppingEvent(OperatorEngine source) {
        super(source);
    }

    public OperatorEngine getOperatorEngine() {
        return (OperatorEngine) getSource();
    }
}
