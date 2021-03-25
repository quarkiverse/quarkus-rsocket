package io.quarkiverse.rsocket.runtime;

import java.util.HashMap;
import java.util.Map;

public class EncoderManager {
    private Map<String, Encoder> encoders = new HashMap<>();
    private Encoder proxy = new ProxyEncoder();

    public EncoderManager() {
        super();
        register(new JsonEncoder());
        register(new CborEncoder());
    }

    public void register(Encoder encoder) {
        encoders.put(encoder.getMimeType(), encoder);
    }

    public Encoder getEncoder(String mime) {
        Encoder encoder = encoders.get(mime);
        return encoder != null? encoder : proxy;
    }
}
