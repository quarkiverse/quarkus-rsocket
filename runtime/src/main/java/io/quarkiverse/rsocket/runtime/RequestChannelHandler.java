package io.quarkiverse.rsocket.runtime;

import reactor.core.publisher.Flux;

public interface RequestChannelHandler<T> {
    Flux<T> handle(Flux<T> payloads);
}
