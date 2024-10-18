package io.github.oleksiyp.operator.engine.client;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.github.oleksiyp.operator.engine.OperatorExecutorHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;

import java.util.List;
import java.util.Objects;

@Configuration
public class KubernetesClientConfiguration {
    private final List<KubernetesClientConfigurer> configurers;
    private final OperatorExecutorHolder executorHolder;

    public KubernetesClientConfiguration(
            @Autowired(required = false)
            List<KubernetesClientConfigurer> configurers,
            OperatorExecutorHolder executorHolder
    ) {
        this.configurers = Objects.requireNonNullElse(configurers, List.of());
        this.executorHolder = executorHolder;
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        KubernetesClientBuilder builder = new KubernetesClientBuilder();

        builder.withTaskExecutor(executorHolder.getExecutorService());

        OrderComparator.sort(configurers);

        for (KubernetesClientConfigurer configurer : configurers) {
            configurer.configure(builder);
        }

        return builder.build();
    }
}
