package io.quarkiverse.rsocket.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.rsocket.Payload;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.util.DefaultPayload;
import io.rsocket.util.EmptyPayload;

public class JsonEncoder implements Encoder {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public Payload encode(Object obj) {
        if (obj instanceof Payload) {
            return (Payload) obj;
        } else {
            // serialize object to payload
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                mapper.writeValue(stream, obj);
                return DefaultPayload.create(stream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                return EmptyPayload.INSTANCE;
            }
        }
    }

    @Override
    public <T> T decode(Payload payload, Class<T> cls) {
        if (Payload.class.isAssignableFrom(cls)) {
            return cls.cast(payload);
        }
        ByteBuffer data = payload.getData();
        try {
            return mapper.<T> readValue(data.array(), cls);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte getMimeTypeId() {
        return WellKnownMimeType.APPLICATION_JSON.getIdentifier();
    }

    @Override
    public String getMimeType() {
        return WellKnownMimeType.APPLICATION_JSON.getString();
    }

}
