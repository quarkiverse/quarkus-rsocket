package io.quarkiverse.rsocket.runtime;

import java.nio.ByteBuffer;
import java.util.Collections;

import org.reactivestreams.Publisher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.AuthMetadataCodec;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.RoutingMetadata;
import io.rsocket.metadata.TaggingMetadata;
import io.rsocket.metadata.TaggingMetadataCodec;
import io.rsocket.metadata.WellKnownAuthType;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.util.DefaultPayload;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class QuarkusRSocketClient implements Disposable {
    private RSocketClient client;
    private String mimeType;
    private String route;
    private WellKnownAuthType authType = null;
    private String username;
    private String password;
    private String token;

    public QuarkusRSocketClient(RSocketClient client) {
        this.client = client;
    }

    public void route(String route) {
        this.route = route;
    }

    public void setOneTimeMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Mono<RSocket> source() {
        return client.source();
    }

    public void authBearer(String token) {
        this.token = token;
        this.authType = WellKnownAuthType.BEARER;
    }

    public void authSimple(String username, String password) {
        this.username = username;
        this.password = password;
        this.authType = WellKnownAuthType.SIMPLE;
    }

    private CompositeByteBuf getMetadata() {
        CompositeByteBuf metadata = ByteBufAllocator.DEFAULT.compositeBuffer();
        if (route != null) {
            RoutingMetadata routingMetadata = TaggingMetadataCodec.createRoutingMetadata(ByteBufAllocator.DEFAULT,
                    Collections.singletonList(route));
            CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                    ByteBufAllocator.DEFAULT,
                    WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
                    routingMetadata.getContent());
        }

        if (mimeType != null) {
            TaggingMetadata mimeMetadata = TaggingMetadataCodec.createTaggingMetadata(ByteBufAllocator.DEFAULT,
                    WellKnownMimeType.MESSAGE_RSOCKET_MIMETYPE.getString(), Collections.singletonList(mimeType));
            CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                    ByteBufAllocator.DEFAULT,
                    WellKnownMimeType.MESSAGE_RSOCKET_MIMETYPE,
                    mimeMetadata.getContent());
            //reset per stream mime type
            mimeType = null;
        }
        if (authType == WellKnownAuthType.SIMPLE) {
            ByteBuf byteBuf = AuthMetadataCodec.encodeSimpleMetadata(ByteBufAllocator.DEFAULT,
                    username.toCharArray(), password.toCharArray());
            CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                    ByteBufAllocator.DEFAULT,
                    WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION,
                    byteBuf);
        }
        if (authType == WellKnownAuthType.BEARER) {
            ByteBuf byteBuf = AuthMetadataCodec.encodeBearerMetadata(ByteBufAllocator.DEFAULT,
                    token.toCharArray());
            CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                    ByteBufAllocator.DEFAULT,
                    WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION,
                    byteBuf);
        }
        return metadata;
    }

    public Mono<Void> fireAndForget(Mono<ByteBuffer> dataMono) {
        return client.fireAndForget(dataMono.map(data -> {
            return DefaultPayload.create(data, getMetadata().nioBuffer());
        }));
    }

    public Mono<Payload> requestResponse(Mono<ByteBuffer> dataMono) {
        return client.requestResponse(dataMono.map(data -> {
            return DefaultPayload.create(data, getMetadata().nioBuffer());
        }));
    }

    public Flux<Payload> requestStream(Mono<ByteBuffer> dataMono) {
        return client.requestStream(dataMono.map(data -> {
            return DefaultPayload.create(data, getMetadata().nioBuffer());
        }));
    }

    public Flux<Payload> requestChannel(Publisher<ByteBuffer> datas) {
        return client.requestChannel(Flux.from(datas).map(data -> {
            return DefaultPayload.create(data, getMetadata().nioBuffer());
        }));
    }

    public Mono<Void> metadataPush(Mono<ByteBuffer> dataMono) {
        return client.metadataPush(dataMono.map(data -> {
            return DefaultPayload.create(data, getMetadata().nioBuffer());
        }));
    }

    public void dispose() {
        client.dispose();
    }
}
