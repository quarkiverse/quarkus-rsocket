package io.quarkiverse.rsocket.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

@QuarkusTest
public class RSocketTestCase {

    @Test
    public void testRSocket() {

        String hello = "Hello RSocket";

        String rsp = RSocketConnector.create()
                //.payloadDecoder(PayloadDecoder.ZERO_COPY)
                .connect(TcpClientTransport.create("127.0.0.1", 7000))
                .block()
                .requestResponse(DefaultPayload.create(hello))
                .block()
                .getDataUtf8();

        Assertions.assertEquals(hello, rsp, "failed to get response");
    }
}
