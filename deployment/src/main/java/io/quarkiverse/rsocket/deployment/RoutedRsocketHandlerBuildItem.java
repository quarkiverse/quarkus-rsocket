package io.quarkiverse.rsocket.deployment;

import io.quarkus.builder.item.MultiBuildItem;
import io.rsocket.frame.FrameType;

public final class RoutedRsocketHandlerBuildItem extends MultiBuildItem {
    private final String path;
    private final String handlerClassName;
    private final FrameType frameType;

    public RoutedRsocketHandlerBuildItem(String path, String handlerClassName, FrameType frameType) {
        this.path = path;
        this.handlerClassName = handlerClassName;
        this.frameType = frameType;
    }

    public String getPath() {
        return path;
    }

    public String getHandler() {
        return handlerClassName;
    }

    public FrameType getFrameType() {
        return frameType;
    }
}
