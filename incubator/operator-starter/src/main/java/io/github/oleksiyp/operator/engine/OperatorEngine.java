package io.github.oleksiyp.operator.engine;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.github.oleksiyp.operator.OperatorController;
import io.github.oleksiyp.operator.engine.events.OperatorStartedEvent;
import io.github.oleksiyp.operator.engine.events.OperatorStartingEvent;
import io.github.oleksiyp.operator.engine.events.OperatorStoppedEvent;
import io.github.oleksiyp.operator.engine.events.OperatorStoppingEvent;
import io.github.oleksiyp.operator.engine.properties.OperatorProperties;
import io.github.oleksiyp.operator.engine.properties.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@EnableConfigurationProperties(OperatorProperties.class)
public class OperatorEngine implements SmartLifecycle {
    private final KubernetesClient kubernetesClient;
    private final List<OperatorController<?>> controllerList;
    private final OperatorProperties properties;
    private final List<AutoCloseable> closeableList = new ArrayList<>();
    private final ApplicationEventPublisher eventPublisher;
    private final OperatorExecutorHolder executorHolder;
    private final Map<Class<? extends HasMetadata>, ResourceHandler> handlers = new LinkedHashMap<>();

    public OperatorEngine(
            KubernetesClient kubernetesClient,
            @Autowired(required = false)
            List<OperatorController<?>> controllerList,
            OperatorProperties properties,
            ApplicationEventPublisher eventPublisher,
            OperatorExecutorHolder executorHolder
    ) {
        this.kubernetesClient = kubernetesClient;
        this.controllerList = Objects.requireNonNullElse(controllerList, List.of());
        this.properties = properties;
        this.eventPublisher = eventPublisher;
        this.executorHolder = executorHolder;
    }

    private void operatorStarting() {
        eventPublisher.publishEvent(new OperatorStartingEvent(this));
    }

    private void operatorStarted() {
        List<ResourceHandler> resourceHandlers;
        synchronized (this) {
            resourceHandlers = new ArrayList<>(handlers.values());
        }
        for (ResourceHandler handler : resourceHandlers) {
            handler.operatorStarted();
        }
        eventPublisher.publishEvent(new OperatorStartedEvent(this));
    }

    private void operatorStopping() {
        eventPublisher.publishEvent(new OperatorStoppingEvent(this));
    }

    private void operatorStopped() {
        eventPublisher.publishEvent(new OperatorStoppedEvent(this));
    }

    @Override
    public void start() {
        executorHolder.getExecutorService()
                .execute(this::operatorStarting);

        Map<? extends Class<? extends HasMetadata>, List<OperatorController<?>>> controllerMap;

        controllerMap = controllerList.stream()
                .collect(Collectors.groupingBy(OperatorController::getResourceType));

        AtomicInteger startedInformers = new AtomicInteger();

        for (Class<? extends HasMetadata> resourceType : controllerMap.keySet()) {
            String resourceTypeName = resourceType.getName();
            ResourceProperties properties = this.properties.getResources()
                    .getOrDefault(resourceTypeName, new ResourceProperties());

            Duration resyncPeriod = properties.getResyncPeriod();

            SharedIndexInformer<HasMetadata> informer;
            informer = (SharedIndexInformer<HasMetadata>) kubernetesClient.resources(resourceType)
                    .runnableInformer(resyncPeriod.toMillis());

            informer.exceptionHandler((isStarted, t) -> {
                log.warn("Operator handling failure for {}", resourceTypeName, t);
                return true;
            });

            closeableList.add(informer);

            List<OperatorController<?>> controllers = controllerMap.get(resourceType);
            ResourceHandler handler = new ResourceHandler(
                    properties,
                    new ControllerDispatcher(kubernetesClient, controllers),
                    executorHolder.getExecutorService(),
                    Clock.systemUTC()
            );

            informer.addEventHandler(handler);

            CompletionStage<Void> stage = informer.start();

            synchronized (this) {
                handlers.put(resourceType, handler);
            }

            stage.thenRun(() -> {
                if (startedInformers.incrementAndGet() != controllerMap.size()) {
                    return;
                }

                executorHolder.getExecutorService()
                        .execute(this::operatorStarted);
            });
        }
    }

    @Override
    public void stop() {
        executorHolder.getExecutorService()
                .execute(this::operatorStopping);

        for (AutoCloseable closeable : closeableList) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("Failed to close {}", closeable, e);
            }
        }
        closeableList.clear();

        executorHolder.getExecutorService()
                .execute(this::operatorStopped);
    }

    @Override
    public boolean isRunning() {
        return !closeableList.isEmpty();
    }
}
