package io.quarkiverse.rsocket.runtime;

import io.rsocket.Payload;
import reactor.core.publisher.Mono;

public interface FireAndForgetHandler {
    Mono<Void> handle(Payload payload);
}
