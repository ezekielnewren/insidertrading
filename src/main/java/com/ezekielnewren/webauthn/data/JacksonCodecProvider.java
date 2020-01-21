/*
 * https://stackoverflow.com/a/47949886/7514786
 */
package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.UncheckedIOException;

public class JacksonCodecProvider implements CodecProvider {
    private final ObjectMapper objectMapper;

    public JacksonCodecProvider(final ObjectMapper bsonObjectMapper) {
        this.objectMapper = bsonObjectMapper;
    }

    @Override
    public <T> Codec<T> get(final Class<T> type, final CodecRegistry registry) {
            return new JacksonCodec<>(objectMapper, registry, type);
    }

    class JacksonCodec<T> implements Codec<T> {
        private final ObjectMapper objectMapper;
        private final CodecRegistry codecRegistry;
        private final Class<T> type;

        public JacksonCodec(ObjectMapper objectMapper,
                            CodecRegistry codecRegistry,
                            Class<T> type) {
            this.objectMapper = objectMapper;
            this.codecRegistry = codecRegistry;
            this.type = type;
        }

        @Override
        public T decode(BsonReader reader, DecoderContext decoderContext) {
            try {
                BsonDocument document = codecRegistry.get(BsonDocument.class).decode(reader, decoderContext);

                // treat _id specially
                BsonValue _id = document.get("_id");
                if (_id != null && _id.getBsonType()==BsonType.OBJECT_ID) {
                    document.put("_id", new BsonString(_id.asObjectId().getValue().toHexString()));
                }

                String json = document.toJson();
                return objectMapper.readValue(json, type);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {
            try {
                String json = objectMapper.writeValueAsString(value);
                BsonDocument raw = BsonDocument.parse(json);

                // treat _id specially
                BsonValue _id = raw.get("_id");
                String tmp = _id.asString().getValue();
                if (_id != null && ObjectId.isValid(tmp)) {
                    raw.put("_id", new BsonObjectId(new ObjectId(tmp)));
                }

                codecRegistry.get(BsonDocument.class).encode(writer, raw, encoderContext);

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Class<T> getEncoderClass() {
            return this.type;
        }
    }

}