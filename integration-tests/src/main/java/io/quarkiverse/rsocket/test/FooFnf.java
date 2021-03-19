package io.quarkiverse.rsocket.test;

import javax.ws.rs.Path;

import io.quarkiverse.rsocket.runtime.RequestResponseHandler;
import io.rsocket.Payload;
import reactor.core.publisher.Mono;

@Path("/foo")
public class FooFnf implements RequestResponseHandler {
    @Override
    public Mono<Payload> handle(Payload payload) {
        try {
            return Mono.just(payload); // reflect the payload back to the sender
        } catch (Exception x) {
            return Mono.error(x);
        }
    }
}
