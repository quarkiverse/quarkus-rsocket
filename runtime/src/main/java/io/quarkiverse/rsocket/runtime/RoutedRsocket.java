package io.quarkiverse.rsocket.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.TaggingMetadata;
import io.rsocket.metadata.WellKnownMimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RoutedRsocket implements RSocket {
    private static final Logger LOGGER = Logger.getLogger(RoutedRsocket.class);
    private final Map<String, RequestResponseHandler> requestResponseRoutes;
    private final Map<String, FireAndForgetHandler> fireAndForgetRoutes;
    private final Map<String, RequestStreamHandler> requestStreamRoutes;
    private final Map<String, RequestChannelHandler> requestChannelRoutes;

    RoutedRsocket(Map<String, RequestResponseHandler> requestResponseRoutes,
            Map<String, FireAndForgetHandler> fireAndForgetRoutes,
            Map<String, RequestStreamHandler> requestStreamRoutes,
            Map<String, RequestChannelHandler> requestChannelRoutes) {
        this.requestResponseRoutes = requestResponseRoutes;
        this.fireAndForgetRoutes = fireAndForgetRoutes;
        this.requestStreamRoutes = requestStreamRoutes;
        this.requestChannelRoutes = requestChannelRoutes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setMimeType(String mimetype) {
        //TODO define decoder/encoder for mime type
    }

    public static final class Builder {
        private final Map<String, RequestResponseHandler> requestResponseRoutes;
        private final Map<String, FireAndForgetHandler> fireAndForgetRoutes;
        private final Map<String, RequestStreamHandler> requestStreamRoutes;
        private final Map<String, RequestChannelHandler> requestChannelRoutes;

        public Builder() {
            this.requestResponseRoutes = new HashMap<>();
            this.fireAndForgetRoutes = new HashMap<>();
            this.requestStreamRoutes = new HashMap<>();
            this.requestChannelRoutes = new HashMap<>();
        }

        public Builder addRequestResponse(String route, String handlerClassName) {
            RequestResponseHandler handler = (RequestResponseHandler) getInstance(handlerClassName);
            requestResponseRoutes.put(route, handler);
            return this;
        }

        public Builder addFireAndForget(String route, String handlerClassName) {
            FireAndForgetHandler handler = (FireAndForgetHandler) getInstance(handlerClassName);
            fireAndForgetRoutes.put(route, handler);
            return this;
        }

        public Builder addRequestStream(String route, String handlerClassName) {
            RequestStreamHandler handler = (RequestStreamHandler) getInstance(handlerClassName);
            requestStreamRoutes.put(route, handler);
            return this;
        }

        public Builder addRequestChannel(String route, String handlerClassName) {
            RequestChannelHandler handler = (RequestChannelHandler) getInstance(handlerClassName);
            requestChannelRoutes.put(route, handler);
            return this;
        }

        public RoutedRsocket build() {
            return new RoutedRsocket(requestResponseRoutes, fireAndForgetRoutes, requestStreamRoutes, requestChannelRoutes);
        }

        private Class<?> loadClass(String className) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                //silent fail
            }
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private Object getInstance(String className) {
            Class<?> classObj = loadClass(className);
            if (classObj == null) {
                return null;
            }

            try {
                return CDI.current().select(classObj).get();
            } catch (UnsatisfiedResolutionException | IllegalStateException e) {
                //silent fail
            }

            try {
                return classObj.getConstructor().newInstance();
            } catch (ReflectiveOperationException | RuntimeException e) {
                return null;
            }
        }
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        try {
            LOGGER.debug("requestResponse called");
            String route = getRoute(payload);
            LOGGER.debug("route :" + route);
            if (route != null) {
                RequestResponseHandler handler = requestResponseRoutes.get(route);
                if (handler != null) {
                    LOGGER.debug("handler found");
                    return handler.handle(payload);
                }
            }
            LOGGER.debug("handler not found");
            return RSocket.super.requestResponse(payload);
        } catch (Throwable t) {
            LOGGER.error("request response error", t);
            return Mono.error(t);
        }
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        try {
            String route = getRoute(payload);
            if (route != null) {
                FireAndForgetHandler handler = fireAndForgetRoutes.get(route);
                if (handler != null) {
                    return handler.handle(payload);
                }
            }
            return RSocket.super.fireAndForget(payload);
        } catch (Throwable t) {
            return Mono.error(t);
        }
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        try {
            String route = getRoute(payload);
            if (route != null) {
                RequestStreamHandler handler = requestStreamRoutes.get(route);
                if (handler != null) {
                    return handler.handle(payload);
                }
            }
            return RSocket.super.requestStream(payload);
        } catch (Throwable t) {
            return Flux.error(t);
        }
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux.from(payloads)
                .switchOnFirst(
                        (signal, flows) -> {
                            Payload payload = null;
                            try {
                                payload = signal.get();
                                if (payload != null) {

                                    String route = getRoute(payload);
                                    if (route != null) {
                                        RequestChannelHandler handler = requestChannelRoutes.get(route);
                                        if (handler != null) {
                                            return handler.handle(payloads);
                                        }
                                    }

                                }
                                return RSocket.super.requestChannel(payloads);
                            } catch (Throwable t) {
                                if (payload != null) {
                                    payload.release();
                                }
                                return Flux.error(t);
                            }
                        },
                        false);

    }

    private Map<String, TaggingMetadata> parseMetadata(Payload payload) {
        Map<String, TaggingMetadata> metadataMap = new HashMap<>();

        if (payload.hasMetadata()) {
            CompositeMetadata compositeMetadata = new CompositeMetadata(payload.metadata(), true);

            for (CompositeMetadata.Entry entry : compositeMetadata) {
                if (entry instanceof CompositeMetadata.WellKnownMimeTypeEntry) {
                    TaggingMetadata metadata = new TaggingMetadata(entry.getMimeType(), entry.getContent());

                    metadataMap.put(entry.getMimeType(), metadata);
                }
            }
        }
        return metadataMap;
    }

    private String getRoute(Payload payload) {
        Map<String, TaggingMetadata> metadatas = parseMetadata(payload);
        TaggingMetadata routeRetadata = metadatas.get(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString());
        if (routeRetadata != null) {
            for (String route : routeRetadata) {
                return route;
            }
        }
        return null;
    }
}
