package io.quarkiverse.rsocket.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

public final class RSocketBuildItem extends SimpleBuildItem {
    private final String rSocketClassName;

    public RSocketBuildItem(String rSocketClassName) {
        this.rSocketClassName = rSocketClassName;
    }

    public String getRSocketClassName() {
        return rSocketClassName;
    }
}
