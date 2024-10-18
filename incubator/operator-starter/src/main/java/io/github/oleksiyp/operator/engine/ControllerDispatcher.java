package io.github.oleksiyp.operator.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.github.oleksiyp.operator.OperatorController;
import io.github.oleksiyp.operator.OperatorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ControllerDispatcher {
    private final KubernetesClient client;
    private final List<OperatorController<?>> controllers;

    public boolean dispatch(
            HasMetadata oldObj,
            HasMetadata newObj,
            boolean delete,
            boolean resync
    ) {
        AtomicBoolean retryFlag = new AtomicBoolean(false);

        if (delete) {
            OperatorEvent<HasMetadata> event = new OperatorEvent<>(newObj, oldObj, resync);
            dispatch(
                    newObj,
                    event,
                    OperatorController::onResourceDeleted,
                    "onResourceDeleted",
                    retryFlag
            );
            return retryFlag.get();
        }

        KubernetesSerialization serialization = client.getKubernetesSerialization();

        ObjectNode keptNewObjCopy = serialization.convertValue(newObj, ObjectNode.class);
        newObj = serialization.clone(newObj);

        if (oldObj == null) {
            OperatorEvent<HasMetadata> event = new OperatorEvent<>(newObj, oldObj, resync);

            dispatch(
                    newObj,
                    event,
                    OperatorController::onResourceChanged,
                    "onResourceChanged",
                    retryFlag
            );
        } else {
            oldObj = serialization.clone(oldObj);

            OperatorEvent<HasMetadata> event = new OperatorEvent<>(newObj, oldObj, resync);

            ObjectNode diffNewObj = serialization.convertValue(newObj, ObjectNode.class);
            ObjectNode diffOldObj = serialization.convertValue(oldObj, ObjectNode.class);

            JsonNode oldMetadata = diffNewObj.remove("metadata");
            JsonNode newMetadata = diffOldObj.remove("metadata");

            cleanResourceVersion(oldMetadata);
            cleanResourceVersion(newMetadata);

            if (!Objects.equals(oldMetadata, newMetadata)) {
                dispatch(
                        newObj,
                        event,
                        OperatorController::onMetadataChanged,
                        "onMetadataChanged",
                        retryFlag
                );
            }

            JsonNode oldStatus = diffNewObj.remove("status");
            JsonNode newStatus = diffOldObj.remove("status");

            if (!Objects.equals(oldStatus, newStatus)) {
                dispatch(
                        newObj,
                        event,
                        OperatorController::onStatusChanged,
                        "onStatusChanged",
                        retryFlag
                );
            }

            if (!Objects.equals(diffNewObj, diffOldObj)) {
                dispatch(
                        newObj,
                        event,
                        OperatorController::onResourceChanged,
                        "onResourceChanged",
                        retryFlag
                );
            }
        }


        JsonNode newObjNow = serialization.convertValue(newObj, JsonNode.class);

        updateStatusIfChanged(newObj, newObjNow, keptNewObjCopy);

        return retryFlag.get();
    }

    private void cleanResourceVersion(JsonNode node) {
        if (node instanceof ObjectNode on) {
            on.remove("resourceVersion");
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatch(
            HasMetadata newObj,
            OperatorEvent<HasMetadata> event,
            BiConsumer<OperatorController<HasMetadata>, OperatorEvent<HasMetadata>> operation,
            String operationName,
            AtomicBoolean retryFlag
    ) {
        for (OperatorController<?> controller : controllers) {
            OperatorController<HasMetadata> c = (OperatorController<HasMetadata>) controller;

            dispatch(
                    newObj,
                    event,
                    it -> operation.accept(c, it),
                    controller::shouldRetryError,
                    operationName,
                    retryFlag
            );
        }
    }

    private void dispatch(
            HasMetadata newObj,
            OperatorEvent<HasMetadata> event,
            Consumer<OperatorEvent<HasMetadata>> operation,
            Function<Throwable, Boolean> shouldRetryError,
            String operationName,
            AtomicBoolean retryFlag
    ) {
        try {
            operation.accept(event);
        } catch (Throwable t) {
            boolean retry = shouldRetryError.apply(t);
            if (retry) {
                retryFlag.set(true);
                log.debug(
                        "Failed to execute {} handler for {}: {} (will retry)",
                        operationName,
                        newObj.getFullResourceName(),
                        nameOf(newObj),
                        t
                );
            } else {
                log.warn(
                        "Failed to execute {} handler for {}: {}",
                        operationName,
                        newObj.getFullResourceName(),
                        nameOf(newObj),
                        t
                );
            }
        }
    }

    private Object nameOf(HasMetadata obj) {
        ObjectMeta metadata = obj.getMetadata();
        if (metadata.getNamespace() == null) {
            return metadata.getName();
        } else {
            return metadata.getNamespace() + "/" + metadata.getName();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateStatusIfChanged(HasMetadata newObj, JsonNode asJsonNode, JsonNode keptCopy) {
        if (Objects.equals(asJsonNode.path("status"), keptCopy.path("status"))) {
            return;
        }

        try {
            Class<HasMetadata> resourceType = (Class<HasMetadata>) newObj.getClass();
            client.resources(resourceType)
                    .resource(newObj)
                    .patchStatus();
        } catch (RuntimeException ex) {
            log.warn(
                    "Failed to handle change of status for {}: {}",
                    newObj.getFullResourceName(),
                    nameOf(newObj),
                    ex
            );
        }
    }
}
