package io.github.jlmc.kafkaexamples.commons.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class GsonSerializer<T> implements Serializer<T> {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public byte[] serialize(final String s, final T t) {
        final String s1 = gson.toJson(t);
        return s1.getBytes(StandardCharsets.UTF_8);
    }
}
