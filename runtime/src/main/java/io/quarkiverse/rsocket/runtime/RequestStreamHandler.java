package io.quarkiverse.rsocket.runtime;

import reactor.core.publisher.Flux;

public interface RequestStreamHandler<T> {
    Flux<T> handle(T payload);
}
