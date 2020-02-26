package com.ezekielnewren.webauthn;

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
 * The JacksonHelper class used to convert JSON to something Mongo can use.
 */
public class JacksonHelper {

    /**
     *
     */
    public static class ObjectIdSerializer extends StdSerializer<ObjectId> {

        /**
         *
         */
        protected ObjectIdSerializer() {
            super(ObjectId.class);
        }

        /**
         * @param _id
         * @param jgen
         * @param provider
         * @throws IOException
         * @throws JsonProcessingException
         */
        @Override
        public void serialize(ObjectId _id, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(_id.toHexString());
        }
    }

    /**
     *
     */
    public static class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

        /**
         *
         */
        protected ObjectIdDeserializer() {
            super(ObjectId.class);
        }

        /**
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
     * @see ObjectMapper
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
