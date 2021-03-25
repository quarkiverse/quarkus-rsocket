package io.quarkiverse.rsocket.runtime;

import io.rsocket.Payload;

public class ProxyEncoder implements Encoder {
    @Override
    public Payload encode(Object obj) {
        if (obj instanceof  Payload)
            return (Payload) obj;
        return null;
    }

    @Override
    public <T> T decode(Payload payload, Class<T> cls) {
        if (Payload.class.isAssignableFrom(cls)) {
            return cls.cast(payload);
        }
        return null;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public byte getMimeTypeId() {
        return 0;
    }
}
