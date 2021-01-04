package io.github.jlmc.kafkaexamples.commons.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GsonDeserializer<T> implements org.apache.kafka.common.serialization.Deserializer<T> {

    public static final String TYPE_CONFIG = "commons.serialization.gsondeserializer.type_config";

    private final Gson gson = new GsonBuilder().create();
    private Class<T> type;

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        String typeName = String.valueOf(configs.get(TYPE_CONFIG));

        try {
            //noinspection unchecked
            this.type = (Class<T>) Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format("Can not find the class [%s] in the classpath", typeName), e);
        }
    }

    @Override
    public T deserialize(final String s, final byte[] bytes) {
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), this.type);
    }
}
