package io.github.oleksiyp.operator.engine;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.github.oleksiyp.operator.engine.properties.ResourceProperties;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ResourceHandler implements ResourceEventHandler<HasMetadata> {
    private final ResourceProperties properties;
    private final ControllerDispatcher dispatcher;
    private final ScheduledExecutorService executorService;
    private final Clock clock;

    private final Map<String, ItemHandler> handlerMap = new LinkedHashMap<>();

    public void operatorStarted() {
        List<ItemHandler> itemHandlers;
        synchronized (this) {
            itemHandlers = new ArrayList<>(handlerMap.values());
        }
        for (ItemHandler itemHandler : itemHandlers) {
            itemHandler.operatorStarted();
        }
    }

    @Override
    public void onAdd(HasMetadata obj) {
        executorService.execute(() -> {
            handlerFor(obj)
                    .onAdd(obj);
        });
    }

    @Override
    public void onUpdate(HasMetadata oldObj, HasMetadata newObj) {
        executorService.execute(() -> {
            handlerFor(newObj)
                    .onUpdate(oldObj, newObj);
        });
    }

    @Override
    public void onDelete(HasMetadata obj, boolean deletedFinalStateUnknown) {
        executorService.execute(() -> {
            handlerFor(obj)
                    .onDelete(obj);
        });
    }

    private synchronized ItemHandler handlerFor(HasMetadata obj) {
        ObjectMeta metadata = obj.getMetadata();
        String namespaceName = Objects.requireNonNullElse(metadata.getNamespace(), "") + "/" + metadata.getName();
        return handlerMap.computeIfAbsent(namespaceName, k -> new ItemHandler());
    }

    private class ItemHandler {
        private HasMetadata newObj;
        private HasMetadata oldObj;
        private boolean delete;
        private boolean resync;
        private boolean started = false;
        private Instant activatedAt;
        private Instant lastActivateAt;
        private long backoffInterval = properties.getBackoffMin().toMillis();
        private Instant backoffReleasedTime;

        public void onAdd(HasMetadata obj) {
            newObj = obj;
            delete = false;
            activate();
            executorService.execute(this::checkIfNeedTrigger);
        }

        public void onUpdate(HasMetadata oldObj, HasMetadata newObj) {
            if (this.oldObj == null && this.newObj == null) {
                this.oldObj = oldObj;
            }
            this.newObj = newObj;
            delete = false;
            activate();
            executorService.execute(this::checkIfNeedTrigger);
        }

        public void onDelete(HasMetadata obj) {
            oldObj = null;
            newObj = obj;
            delete = true;
            activate();
            executorService.execute(this::checkIfNeedTrigger);
        }

        private void activate() {
            Instant time = clock.instant();
            if (activatedAt == null) {
                activatedAt = time;
            }
            lastActivateAt = time;
        }

        public void operatorStarted() {
            started = true;
            executorService.execute(this::checkIfNeedTrigger);
        }

        private void checkIfNeedTrigger() {
            Instant now = clock.instant();

            long passed = activatedAt.until(now, ChronoUnit.MILLIS);
            long passedLast = lastActivateAt.until(now, ChronoUnit.MILLIS);

            long delay = Long.MIN_VALUE;
            if (backoffReleasedTime != null) {
                delay = now.until(backoffReleasedTime, ChronoUnit.MILLIS);
            }

            long delayMin = Math.max(properties.getMinDebounce().toMillis() - passedLast, delay);
            long delayMax = Math.max(properties.getMaxDebounce().toMillis() - passed, delay);

            if (started && (delayMin <= 0 || delayMax <= 0)) {
                execute();
                return;
            }

            delay = Math.min(delayMin, delayMax);
            if (delay >= 0) {
                executorService.schedule(this::checkIfNeedTrigger, delay, TimeUnit.MILLISECONDS);
            }
        }


        public void execute() {
            boolean shouldRetry = dispatcher.dispatch(oldObj, newObj, delete, resync);
            if (shouldRetry) {
                penalize();
            } else {
                reset();
            }
        }

        private void penalize() {
            backoffInterval = (long) Math.min(
                    properties.getBackoffMax().toMillis(),
                    properties.getBackoffFactor() * backoffInterval
            );
            backoffReleasedTime = clock.instant().plusMillis(backoffInterval);
            executorService.execute(this::checkIfNeedTrigger);
        }

        private void reset() {
            oldObj = null;
            newObj = null;
            backoffReleasedTime = null;
            delete = false;
            activatedAt = null;
            lastActivateAt = null;
        }
    }
}
