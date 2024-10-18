package io.github.oleksiyp.operator;

public record OperatorEvent<T>(T resource, T oldResource, boolean resync) {
}
