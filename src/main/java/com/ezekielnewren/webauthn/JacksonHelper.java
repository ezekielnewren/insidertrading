package com.ezekielnewren.webauthn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JacksonHelper {


    public static ObjectMapper newObjectMapper() {
        return new ObjectMapper()
        .registerModule(new Jdk8Module())
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }


}
