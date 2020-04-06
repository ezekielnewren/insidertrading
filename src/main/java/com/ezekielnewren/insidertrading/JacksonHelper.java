package com.ezekielnewren.insidertrading;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * The JacksonHelper class that assists Jackson by serializing, deserializing and mapping object.
 */
public class JacksonHelper {

    //used only on objectId?
    /**
     * The {@code ObjectIdSerializer} class that handles serialization for {@link com.ezekielnewren.insidertrading.JacksonHelper}.
     * @see com.fasterxml.jackson.databind.ser.std.StdSerializer
     */
    public static class ObjectIdSerializer extends StdSerializer<ObjectId> {


        /**
         * Constructor takes parent {@code ObjectId} reference.
         * @see org.bson.types.ObjectId
         */
        protected ObjectIdSerializer() {
            super(ObjectId.class);
        }

        /**
         * Generates a {@code JSON} with the id converted to hex.
         * @param _id generated user id.
         * @param jgen generator used for writing {@code JSON}.
         * @param provider the 'blueprint' for how to serialize.
         * @throws IOException stream is interrupted or data is corrupt.
         * @throws JsonProcessingException throws when parsing or generating {@code JSON}, never caught.
         * @see org.bson.types.ObjectId
         * @see com.fasterxml.jackson.core.JsonGenerator
         * @see com.fasterxml.jackson.databind.SerializerProvider
         */
        @Override
        public void serialize(ObjectId _id, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(_id.toHexString());
        }
    }

    /**
     * The {@code ObjectIdDeserializer} handles deserialization for {@link com.ezekielnewren.insidertrading.JacksonHelper}.
     * @see com.fasterxml.jackson.databind.deser.std.StdDeserializer
     */
    public static class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

        /**
         * Constructor takes the parent {@code ObjectId} reference.
         * @see org.bson.types.ObjectId
         */
        protected ObjectIdDeserializer() {
            super(ObjectId.class);
        }

        //not sure if this is correct
        /**
         * Deserializes {@code JSON} payload.
         * @param p data to be parsed.
         * @param ctxt value to be deserialized.
         * @return string of {@code ObjectId}.
         * @throws IOException stream is interrupted or data is corrupt.
         * @throws JsonProcessingException throws when parsing or generating {@code JSON}.
         * @see org.bson.types.ObjectId
         * @see com.fasterxml.jackson.core.JsonParser
         * @see com.fasterxml.jackson.databind.DeserializationContext
         */
        @Override
        public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return new ObjectId(p.getValueAsString());
        }
    }

    /**
     * Used to create an {@code ObjectMapper} that will work with {@code Jackson} and {@code MongoDB}
     * @return {@code ObjectMapper} that works with {@code MongoDB}.
     * @see com.fasterxml.jackson.databind.ObjectMapper
     */
    public static ObjectMapper newObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ObjectIdSerializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new Jdk8Module());
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        om.registerModule(module);

        return om;
    }

    /**
     * Converts the serialized {@code JSON} to {@code BSON}.
     * @param om {@code ObjectMapper} containing data from {@code MongoDB}.
     * @param o value to be encoded.
     * @return generated {@code BSON} data.
     * @throws JsonProcessingException when parsing {@code JSON}.
     */
    public static BsonDocument toBsonDocument(ObjectMapper om, Object o) throws JsonProcessingException {
        String json = om.writeValueAsString(o);
        BsonDocument raw = BsonDocument.parse(json);

        // treat _id specially
        BsonValue _id = raw.get("_id");
        String tmp = null;
        if (_id != null && ObjectId.isValid(tmp = _id.asString().getValue())) {
            raw.put("_id", new BsonObjectId(new ObjectId(tmp)));
        }

        return raw;
    }


}
