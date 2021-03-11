package io.quarkiverse.rsocket.runtime;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.rsocket.frame.FrameType;

@Recorder
public class RSocketRecorder {

    public RuntimeValue<NettyRSocketServer> initServer(RSocketConfig config, String rsocketClassName) {
        NettyRSocketServer server = new NettyRSocketServer();
        server.start(config, rsocketClassName);
        return new RuntimeValue<>(server);
    }

    public RuntimeValue<RoutedRsocket.Builder> buildRouter() {
        return new RuntimeValue<>(RoutedRsocket.builder());
    }

    public void addHandler(RuntimeValue<RoutedRsocket.Builder> builder, String route, String handlerClassName,
            FrameType frameType) {
        if (frameType == FrameType.REQUEST_FNF) {
            builder.getValue().addFireAndForget(route, handlerClassName);
        } else if (frameType == FrameType.REQUEST_CHANNEL) {
            builder.getValue().addRequestChannel(route, handlerClassName);
        } else if (frameType == FrameType.REQUEST_RESPONSE) {
            builder.getValue().addRequestResponse(route, handlerClassName);
        } else if (frameType == FrameType.REQUEST_STREAM) {
            builder.getValue().addRequestStream(route, handlerClassName);
        }
    }

    public RuntimeValue<NettyRSocketServer> initServer(RSocketConfig config, RuntimeValue<RoutedRsocket.Builder> builder) {
        NettyRSocketServer server = new NettyRSocketServer();
        server.start(config, builder.getValue().build());
        return new RuntimeValue<>(server);
    }
}
