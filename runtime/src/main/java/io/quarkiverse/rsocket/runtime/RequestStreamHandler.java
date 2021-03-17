package io.quarkiverse.rsocket.runtime;

import io.rsocket.Payload;
import reactor.core.publisher.Flux;

public interface RequestStreamHandler {
    Flux<Payload> handle(Payload payload);
}
