package io.quarkiverse.rsocket.runtime;

import java.util.HashMap;
import java.util.Map;

import io.rsocket.metadata.WellKnownMimeType;

public class EncoderManager {
    private Map<String, Encoder> encoders = new HashMap<>();

    public EncoderManager() {
        super();
        register(new JsonEncoder());
    }

    public void register(Encoder encoder) {
        encoders.put(encoder.getMimeType(), encoder);
    }

    public Encoder getDefaultEncoder() {
        return encoders.get(WellKnownMimeType.APPLICATION_JSON.getString());
    }

    public Encoder getEncoder(String mime) {
        return encoders.get(mime);
    }
}
