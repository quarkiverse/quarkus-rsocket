package io.quarkiverse.rsocket.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = RSocketConfig.CONFIG_NAME, phase = ConfigPhase.RUN_TIME)
public class RSocketConfig {
    public static final String CONFIG_NAME = "rsocket";
    /**
     * Rsocket server port
     */
    @ConfigItem(defaultValue = "7000")
    public int port;

    /**
     * Rsocket server host
     */
    @ConfigItem(defaultValue = "localhost")
    public String host;

    /**
     * Rsocket server protocole (TCP or WebSocket)
     */
    @ConfigItem(defaultValue = "TCP")
    public TransportProtocol transportProtocol;

    public enum TransportProtocol {
        TCP,
        WEB_SOCKET
    }
}
