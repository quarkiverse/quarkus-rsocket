package io.quarkiverse.rsocket.test;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import reactor.core.publisher.Mono;

public class MyRsocketImpl implements RSocket {
    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        try {
            return Mono.just(payload); // reflect the payload back to the sender
        } catch (Exception x) {
            return Mono.error(x);
        }
    }

}
