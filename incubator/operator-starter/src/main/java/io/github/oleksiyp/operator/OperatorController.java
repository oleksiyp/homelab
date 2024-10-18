package io.github.oleksiyp.operator;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface OperatorController<T extends HasMetadata> {
    @SuppressWarnings("unchecked")
    default Class<T> getResourceType() {
        Class<?> cls = this.getClass();
        while (cls != null) {
            for (Type interfaces : cls.getGenericInterfaces()) {
                if (!(interfaces instanceof ParameterizedType parameterizedType)) {
                    continue;
                }
                if (!parameterizedType.getRawType().equals(OperatorController.class)) {
                    continue;
                }
                return (Class<T>) parameterizedType.getActualTypeArguments()[0];
            }
            if (cls.getGenericSuperclass() instanceof Class<?> superclass) {
                cls = superclass;
            } else {
                cls = null;
            }
        }
        throw new RuntimeException("failed to deduce resource type from inheritance for " + this.getClass());
    }

    void onResourceChanged(OperatorEvent<T> event);

    default void onResourceDeleted(OperatorEvent<T> event) {
    }

    default void onMetadataChanged(OperatorEvent<T> event) {
    }

    default void onStatusChanged(OperatorEvent<T> event) {
    }

    default boolean shouldRetryError(Throwable t) {
        return true;
    }
}
