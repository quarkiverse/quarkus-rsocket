package io.quarkiverse.rsocket.runtime;

import org.reactivestreams.Publisher;

import io.rsocket.Payload;
import reactor.core.publisher.Flux;

public interface RequestChannelHandler {
    Flux<Payload> handle(Publisher<Payload> payloads);
}
