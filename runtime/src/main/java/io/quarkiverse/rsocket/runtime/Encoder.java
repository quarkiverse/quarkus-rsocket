package io.quarkiverse.rsocket.runtime;

import io.rsocket.Payload;

public interface Encoder {
    Payload encode(Object obj);

    <T> T decode(Payload payload, Class<T> cls);

    String getMimeType();

    byte getMimeTypeId();
}
