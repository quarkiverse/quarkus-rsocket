package io.quarkiverse.rsocket.runtime;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketClient;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.lease.LeaseStats;
import io.rsocket.lease.Leases;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class QuarkusRSocketConnector {
    // can not extends RSocketConnector because constructor is private and not protected
    private final RSocketConnector proxy;

    private QuarkusRSocketConnector() {
        proxy = RSocketConnector.create();
        //add composite metadata extension by default
        proxy.metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());
        proxy.dataMimeType(WellKnownMimeType.APPLICATION_JSON.getString());
    }

    public static QuarkusRSocketConnector create() {
        return new QuarkusRSocketConnector();
    }

    public static QuarkusRSocketClient connectWith(ClientTransport transport) {
        return create().connect(() -> {
            return transport;
        });
    }

    public QuarkusRSocketConnector setupPayload(Mono<Payload> setupPayloadMono) {
        proxy.setupPayload(setupPayloadMono);
        return this;
    }

    public QuarkusRSocketConnector setupPayload(Payload payload) {
        proxy.setupPayload(payload);
        return this;
    }

    public QuarkusRSocketConnector dataMimeType(String dataMimeType) {
        proxy.dataMimeType(dataMimeType);
        return this;
    }

    public QuarkusRSocketConnector json() {
        proxy.dataMimeType(WellKnownMimeType.APPLICATION_JSON.getString());
        return this;
    }

    public QuarkusRSocketConnector cbor() {
        proxy.dataMimeType(WellKnownMimeType.APPLICATION_CBOR.getString());
        return this;
    }

    public QuarkusRSocketConnector protobuf() {
        proxy.dataMimeType(WellKnownMimeType.APPLICATION_PROTOBUF.getString());
        return this;
    }

    public QuarkusRSocketConnector xml() {
        proxy.dataMimeType(WellKnownMimeType.APPLICATION_XML.getString());
        return this;
    }

    public QuarkusRSocketConnector binary() {
        proxy.dataMimeType("application/binary");
        return this;
    }

    public QuarkusRSocketConnector metadataMimeType(String metadataMimeType) {
        proxy.metadataMimeType(metadataMimeType);
        return this;
    }

    public QuarkusRSocketConnector keepAlive(Duration interval, Duration maxLifeTime) {
        proxy.keepAlive(interval, maxLifeTime);
        return this;
    }

    public QuarkusRSocketConnector interceptors(Consumer<InterceptorRegistry> configurer) {
        proxy.interceptors(configurer);
        return this;
    }

    public QuarkusRSocketConnector acceptor(SocketAcceptor acceptor) {
        proxy.acceptor(acceptor);
        return this;
    }

    public QuarkusRSocketConnector reconnect(Retry retry) {
        proxy.reconnect(retry);
        return this;
    }

    public QuarkusRSocketConnector resume(Resume resume) {
        proxy.resume(resume);
        return this;
    }

    public QuarkusRSocketConnector lease(Supplier<Leases<? extends LeaseStats>> supplier) {
        proxy.lease(supplier);
        return this;
    }

    public QuarkusRSocketConnector maxInboundPayloadSize(int maxInboundPayloadSize) {
        proxy.maxInboundPayloadSize(maxInboundPayloadSize);
        return this;
    }

    public QuarkusRSocketConnector fragment(int mtu) {
        proxy.fragment(mtu);
        return this;
    }

    public QuarkusRSocketConnector payloadDecoder(PayloadDecoder decoder) {
        proxy.payloadDecoder(decoder);
        return this;
    }

    public QuarkusRSocketClient connect(ClientTransport transport) {
        return this.connect(() -> {
            return transport;
        });
    }

    public QuarkusRSocketClient tcp(String host, int port) {
        return connect(TcpClientTransport.create(host, port));
    }

    public QuarkusRSocketClient webSocket(String host, int port) {
        return connect(WebsocketClientTransport.create(host, port));
    }

    public QuarkusRSocketClient connect(Supplier<ClientTransport> transportSupplier) {
        return new QuarkusRSocketClient(RSocketClient.from(proxy.connect(transportSupplier)));
    }
}
