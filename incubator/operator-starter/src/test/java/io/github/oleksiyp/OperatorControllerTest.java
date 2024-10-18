package io.github.oleksiyp;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.github.oleksiyp.operator.OperatorController;
import io.github.oleksiyp.operator.OperatorEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OperatorControllerTest {
    @Test
    public void checkDefaultResourceType() {
        Controller1 controller = new Controller1();
        Assertions.assertEquals(Resource.class, controller.getResourceType());
    }

    @Test
    public void checkDefaultResourceTypeWithInheritance() {
        Controller2 controller = new Controller2();
        Assertions.assertEquals(Resource.class, controller.getResourceType());
    }

    static class Resource implements HasMetadata {
        @Override
        public ObjectMeta getMetadata() {
            return null;
        }

        @Override
        public void setMetadata(ObjectMeta objectMeta) {

        }

        @Override
        public void setApiVersion(String s) {

        }
    }

    static class Controller1 implements OperatorController<Resource> {
        @Override
        public void onResourceChanged(OperatorEvent<Resource> event) {

        }
    }

    static class Controller2 extends Controller1 {
    }
}