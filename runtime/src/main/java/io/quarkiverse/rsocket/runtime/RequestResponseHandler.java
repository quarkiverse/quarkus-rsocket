package io.quarkiverse.rsocket.runtime;

import reactor.core.publisher.Mono;

public interface RequestResponseHandler<T> {
    Mono<T> handle(T payload);
}
