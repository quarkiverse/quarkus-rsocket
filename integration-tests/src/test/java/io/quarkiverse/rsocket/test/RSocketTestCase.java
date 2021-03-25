package io.quarkiverse.rsocket.test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import io.quarkiverse.rsocket.runtime.JsonEncoder;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.quarkus.test.junit.QuarkusTest;
import io.rsocket.Payload;
import io.rsocket.core.RSocketConnector;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.RoutingMetadata;
import io.rsocket.metadata.TaggingMetadataCodec;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

@QuarkusTest
public class RSocketTestCase {
    private static final Logger LOGGER = Logger.getLogger(RSocketTestCase.class);

    @Test
    public void testRSocketRoute() {
        String hello = "Hello RSocket";
        CompositeByteBuf metadata = ByteBufAllocator.DEFAULT.compositeBuffer();
        RoutingMetadata routingMetadata = TaggingMetadataCodec.createRoutingMetadata(ByteBufAllocator.DEFAULT,
                Collections.singletonList("/foo"));
        CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                ByteBufAllocator.DEFAULT,
                WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
                routingMetadata.getContent());
        ByteBuf data = ByteBufAllocator.DEFAULT.buffer().writeBytes(hello.getBytes(Charsets.UTF_8));
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(hello));
        Payload rspPayload = RSocketConnector.create()
                .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())
                //.payloadDecoder(PayloadDecoder.ZERO_COPY)
                .connect(TcpClientTransport.create("127.0.0.1", 7000))
                .block()
                .requestResponse(DefaultPayload.create(byteBuffer, metadata.nioBuffer()))
                .block();
        metadata.release();
        Assertions.assertEquals(hello, rspPayload.getDataUtf8(), "failed to get response");
    }

    @Test
    public void testRSocketEncoder() {

        CompositeByteBuf metadata = ByteBufAllocator.DEFAULT.compositeBuffer();
        RoutingMetadata routingMetadata = TaggingMetadataCodec.createRoutingMetadata(ByteBufAllocator.DEFAULT,
                Collections.singletonList("/foo"));
        CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                ByteBufAllocator.DEFAULT,
                WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
                routingMetadata.getContent());
        Car c = new Car();
        c.setName("batmobile");
        JsonEncoder encoder = new JsonEncoder();
        Payload payload = encoder.encode(c);
        Payload rspPayload = RSocketConnector.create()
                .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())
                //.payloadDecoder(PayloadDecoder.ZERO_COPY)
                .connect(TcpClientTransport.create("127.0.0.1", 7000))
                .block()
                .requestResponse(DefaultPayload.create(payload.getData(), metadata.nioBuffer()))
                .block();
        metadata.release();
        Assertions.assertEquals("{\"name\":\"batmobile\"}", rspPayload.getDataUtf8(), "failed to get response");
    }
}
