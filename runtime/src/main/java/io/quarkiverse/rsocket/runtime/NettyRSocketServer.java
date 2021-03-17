package io.quarkiverse.rsocket.runtime;

import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;

public class NettyRSocketServer {
    private static final Logger LOGGER = Logger.getLogger(NettyRSocketServer.class);
    private RSocketServer server;

    public void start(RSocketConfig config, String rsocketClassName) {
        RSocket rsocket = (RSocket) getInstance(rsocketClassName);
        LOGGER.info("recorder start " + rsocketClassName);
        start(config, rsocket);
    }

    public void start(RSocketConfig config, RSocket rsocket) {
        server = RSocketServer.create();
        server.acceptor((setupPayload, reactiveSocket) -> {
            String mimetype = setupPayload.dataMimeType();
            if (rsocket instanceof RoutedRsocket) {
                RoutedRsocket routedSocket = (RoutedRsocket) rsocket;
                routedSocket.setMimeType(mimetype);
            }
            return Mono.just(rsocket);
        });
        if (config.transportProtocol == RSocketConfig.TransportProtocol.TCP) {
            LOGGER.info("TCP MODE");
            TcpServer tcpServer = TcpServer.create().host(config.host).port(config.port);
            //TODO SSL support
            //SslContextBuilder builder;
            //tcpServer.secure((contextSpec) -> contextSpec.sslContext(builder.build()));
            TcpServerTransport transport = TcpServerTransport.create(tcpServer);
            server.bind(transport).block();
        } else {
            LOGGER.info("WEBSOCKET MODE");
            server.bind(WebsocketServerTransport.create(config.host, config.port)).block();
        }

        LOGGER.info("recorder server started ");
        //TODO
        //server.interceptors(null);
        //server.lease(null);
        //server.resume(null);
        //server.fragment(0);
        //server.maxInboundPayloadSize(Integer.MAX_VALUE);
        //server.payloadDecoder(null);

    }

    private Class<?> loadClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            //silent fail
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("failed to load class " + className);
            return null;
        }
    }

    private Object getInstance(String className) {
        Class<?> classObj = loadClass(className);
        if (classObj == null) {
            LOGGER.info("failed to load class :" + className);
            return null;
        }

        try {
            return CDI.current().select(classObj).get();
        } catch (UnsatisfiedResolutionException | IllegalStateException e) {
            //silent fail
        }

        try {
            return classObj.getConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

}
