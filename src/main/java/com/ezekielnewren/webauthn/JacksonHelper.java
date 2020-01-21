package com.ezekielnewren.webauthn;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.yubico.internal.util.json.JsonStringSerializable;
import com.yubico.webauthn.data.ByteArray;
import org.bson.types.ObjectId;

import java.io.IOException;

public class JacksonHelper {

//    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
//            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
//            setterVisibility = JsonAutoDetect.Visibility.NONE,
//            creatorVisibility = JsonAutoDetect.Visibility.NONE,
//            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
//    )
//    public static abstract class MixinObjectId implements JsonStringSerializable {
//        @JsonCreator
//        public MixinObjectId(@JsonProperty("_id") String _id) {}
//        @JsonProperty("_id") public abstract String toString();
//
//        @Override
//        public String toJsonString() {
//            return null;
//        }
//    }

    public static class ObjectIdSerializer extends StdSerializer<ObjectId> {
        protected ObjectIdSerializer() {
            super(ObjectId.class);
        }

        @Override
        public void serialize(ObjectId _id, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(_id.toHexString());
        }
    }

    public static class ObjectIdDeserializer extends StdDeserializer<ObjectId> {
        protected ObjectIdDeserializer() {
            super(ObjectId.class);
        }

        @Override
        public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return new ObjectId(p.getValueAsString());
        }
    }




    public static ObjectMapper newObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ObjectIdSerializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());

        return new ObjectMapper()
        .registerModule(new Jdk8Module())
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        //.addMixIn(ObjectId.class, MixinObjectId.class)
        .registerModule(module);
    }


}
