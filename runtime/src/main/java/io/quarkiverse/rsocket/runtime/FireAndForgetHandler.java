package io.quarkiverse.rsocket.runtime;

import reactor.core.publisher.Mono;

public interface FireAndForgetHandler<T> {
    Mono<Void> handle(T payload);
}
