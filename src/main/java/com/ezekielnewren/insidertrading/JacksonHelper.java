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
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * The JacksonHelper class that assists Jackson by serializing, deserializing and mapping object.
 */
public class JacksonHelper {

    //used only on objectId?
    /**
     * The ObjectIdSerializer class that handles serialization for JacksonHelper.
     */
    public static class ObjectIdSerializer extends StdSerializer<ObjectId> {


        /**
         * Constructor takes parent ObjectId reference.
         */
        protected ObjectIdSerializer() {
            super(ObjectId.class);
        }

        /**
         * Generates a JSON with the id converted to hex.
         * @param _id generated user id.
         * @param jgen generator used for writing JSON.
         * @param provider the 'blueprint' for how to serialize.
         * @throws IOException throws a new I/O exception.
         * @throws JsonProcessingException throws an exception if one occurs when parsing or generating JSON.
         */
        @Override
        public void serialize(ObjectId _id, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(_id.toHexString());
        }
    }

    /**
     * The ObjectIdDeserializer handles deserialization for JacksonHelper.
     */
    public static class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

        /**
         * Constructor takes the parent ObjectId reference.
         */
        protected ObjectIdDeserializer() {
            super(ObjectId.class);
        }

        /**
         *
         * @param p
         * @param ctxt
         * @return
         * @throws IOException
         * @throws JsonProcessingException
         */
        @Override
        public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return new ObjectId(p.getValueAsString());
        }
    }

    /**
     * @return
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


}
