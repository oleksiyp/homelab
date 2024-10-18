package io.github.oleksiyp;

import io.github.oleksiyp.operator.OperatorController;
import io.github.oleksiyp.operator.OperatorEvent;
import io.github.oleksiyp.v2.Example;
import org.springframework.stereotype.Component;

@Component
public class ExampleV2Controller implements OperatorController<Example> {
    @Override
    public void onResourceChanged(OperatorEvent<Example> event) {
        System.out.println(event.resource());
    }
}
