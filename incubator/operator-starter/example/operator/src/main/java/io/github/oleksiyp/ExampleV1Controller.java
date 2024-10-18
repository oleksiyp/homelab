package io.github.oleksiyp;

import io.github.oleksiyp.operator.OperatorController;
import io.github.oleksiyp.operator.OperatorEvent;
import io.github.oleksiyp.v1.Example;
import io.github.oleksiyp.v1.ExampleStatus;
import org.springframework.stereotype.Component;

@Component
public class ExampleV1Controller implements OperatorController<Example> {
    @Override
    public void onResourceChanged(OperatorEvent<Example> event) {
        Example r = event.resource();
        ExampleStatus status = getOrCreateStatus(r);
        status.setCurrentSize(
                status.getCurrentSize() + r.getSpec().getSize()
        );
    }

    private static ExampleStatus getOrCreateStatus(Example r) {
        ExampleStatus status = r.getStatus();
        if (status == null) {
            status = new ExampleStatus();
            r.setStatus(status);
        }
        return status;
    }

    @Override
    public void onMetadataChanged(OperatorEvent<Example> event) {
        System.out.println("metadata changed");
    }

    @Override
    public void onStatusChanged(OperatorEvent<Example> event) {
        System.out.println("status changed");
    }
}
