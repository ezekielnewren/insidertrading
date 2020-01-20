package com.ezekielnewren.webauthn;

import com.ezekielnewren.webauthn.data.JacksonCodecProvider;
import com.ezekielnewren.webauthn.data.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Value;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

@Value
public class AuthServletContext {
    final Object mutex = new Object();

    ObjectMapper objectMapper;

    MongoClient client;
    MongoDatabase database;
    MongoCollection<User> collectionUser;
    MongoCollection<Document> collectionData;

    WebAuthn webAuthn;

    public AuthServletContext(ObjectMapper _om, String connectionString, MongoCredential _cred, String fqdn, String title) {
        this.objectMapper = _om;

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(new JacksonCodecProvider(objectMapper)));

        ConnectionString connect = new ConnectionString(connectionString);
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyConnectionString(connect)
                .codecRegistry(codecRegistry);
        if (_cred != null) builder.credential(_cred);
        MongoClientSettings mcs = builder.build();
        this.client = MongoClients.create(mcs);
        this.database = client.getDatabase("tomcat");
        this.collectionUser = database.getCollection("user", User.class);
        this.collectionData = database.getCollection("data");

        this.webAuthn = new WebAuthn(this, fqdn, title);
    }


}
