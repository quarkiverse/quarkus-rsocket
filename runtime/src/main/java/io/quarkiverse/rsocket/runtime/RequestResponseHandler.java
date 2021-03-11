package io.quarkiverse.rsocket.runtime;

import io.rsocket.Payload;
import reactor.core.publisher.Mono;

public interface RequestResponseHandler {
    Mono<Payload> handle(Payload payload);
}
