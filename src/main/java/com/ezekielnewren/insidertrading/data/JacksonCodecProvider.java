/*
 * https://stackoverflow.com/a/47949886/7514786
 */
package com.ezekielnewren.insidertrading.data;

import com.ezekielnewren.insidertrading.JacksonHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The {@code JacksonCodecProvider} class contains both the {@code Provider} and {@code Codec}.
 * @see org.bson.codecs.configuration.CodecProvider
 */
public class JacksonCodecProvider implements CodecProvider {

    /**
     * Constant {@code ObjectMapper} variable.
     * @see com.fasterxml.jackson.databind.ObjectMapper
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code ObjectMapper} equal to provided {@code BSON ObjectMapper}.
     * @param bsonObjectMapper
     * @see com.fasterxml.jackson.databind.ObjectMapper
     */
    public JacksonCodecProvider(final ObjectMapper bsonObjectMapper) {
        this.objectMapper = bsonObjectMapper;
    }

    /**
     * Method that can both encode and decode values.
     * @param type type.
     * @param registry registry.
     * @param <T> type of class for the model
     * @return
     * @see org.bson.codecs.Codec
     * @see java.lang.Class
     * @see org.bson.codecs.configuration.CodecRegistry
     */
    @Override
    public <T> Codec<T> get(final Class<T> type, final CodecRegistry registry) {
            return new JacksonCodec<>(objectMapper, registry, type);
    }

    /**
     * Contains information for the {@code Codec}.
     * @param <T> type class for the model.
     * @see org.bson.codecs.Codec
     */
    class JacksonCodec<T> implements Codec<T> {

        /**
         * Constant{@code ObjectMapper} object.
         * @see com.fasterxml.jackson.databind.ObjectMapper
         */
        private final ObjectMapper objectMapper;

        /**
         * Constant {@code CodecRegistry} object.
         * @see org.bson.codecs.configuration.CodecRegistry
         */
        private final CodecRegistry codecRegistry;

        /**
         * Constant {@code Class<T>} object.
         * @see java.lang.Class
         */
        private final Class<T> type;

        /**
         * Constant object that contains {@code JSONWriter} settings.
         * @see org.bson.json.JsonWriterSettings
         */
        private final JsonWriterSettings plainJson = JsonWriterSettings.builder()
                .objectIdConverter((value, writer)->writer.writeString(value.toHexString()))
                .int64Converter((value, writer)->writer.writeNumber(value.toString()))
                .build();

        /**
         * Constructor for the codec.
         * @param objectMapper {@code ObjectMapper}, used for reading and writing {@code JSON}
         * @param codecRegistry registry
         * @param type type for class
         * @see com.fasterxml.jackson.databind.ObjectMapper
         * @see org.bson.codecs.configuration.CodecRegistry
         * @see java.lang.Class
         */
        public JacksonCodec(ObjectMapper objectMapper,
                            CodecRegistry codecRegistry,
                            Class<T> type) {
            this.objectMapper = objectMapper;
            this.codecRegistry = codecRegistry;
            this.type = type;
        }

        /**
         * Method to decode {@code MongoDB BSON} to Java objects
         * @param reader the BSON reader.
         * @param decoderContext the decoder context.
         * @return Java object.
         * @see org.bson.BsonReader
         * @see org.bson.codecs.DecoderContext
         */
        @Override
        public T decode(BsonReader reader, DecoderContext decoderContext) {
            try {
                BsonDocument document = codecRegistry.get(BsonDocument.class).decode(reader, decoderContext);

                String json = document.toJson(plainJson);
                return objectMapper.readValue(json, type);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Method for encoding {@code JSON} to {@code BSON}.
         * @param writer the {@code BSON} writer to encode into.
         * @param value the value to encode.
         * @param encoderContext the encoder context.
         * @see org.bson.BsonWriter
         * @see java.lang.Object
         * @see org.bson.codecs.EncoderContext
         */
        @Override
        public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {
            try {
                BsonDocument raw = JacksonHelper.toBsonDocument(objectMapper, value);

                codecRegistry.get(BsonDocument.class).encode(writer, raw, encoderContext);

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Method to get the {@code Class<T>} type.
         * @return the {@code Class<T>} type.
         * @see java.lang.Class
         */
        @Override
        public Class<T> getEncoderClass() {
            return this.type;
        }
    }

}