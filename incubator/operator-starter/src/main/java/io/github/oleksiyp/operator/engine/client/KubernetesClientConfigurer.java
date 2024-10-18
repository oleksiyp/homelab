package io.github.oleksiyp.operator.engine.client;

import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public interface KubernetesClientConfigurer {
    void configure(KubernetesClientBuilder builder);
}
