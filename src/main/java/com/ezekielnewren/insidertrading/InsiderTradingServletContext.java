package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.JacksonCodecProvider;
import com.ezekielnewren.insidertrading.data.User;
import com.ezekielnewren.insidertrading.data.UserStore;
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

/**
 *
 */
@Value
@Getter
public class InsiderTradingServletContext {

    /**
     * Creating a new constant mutex object.
     * @see Object
     */
    final Object mutex = new Object();

    /**
     * Creating an objectMapper variable
     * @see ObjectMapper
     */
    ObjectMapper objectMapper;

    /**
     * Declare a new client
     * @see MongoClient
     */
    MongoClient client;

    /**
     * Declare a new database
     * @see MongoDatabase
     */
    MongoDatabase database;
//<<<<<<< HEAD
//
//    /**
//     * Declare a new collection
//     * @see MongoCollection
//     */
//    MongoCollection<User> collectionUser;
//
//    /**
//     *
//     * @see MongoCollection
//     */
//    MongoCollection<Document> collectionData;
//=======

    public MongoCollection<User> collectionUser;
    public MongoCollection<Document> collectionData;


    /**
     *
     */
    WebAuthn webAuthn;

    /**
     *
     */
    UserStore userStore;

    /**
     * @param _om
     * @param connectionString
     * @param _cred
     * @param fqdn
     * @param title
     */
    public InsiderTradingServletContext(ObjectMapper _om, String connectionString, MongoCredential _cred, String fqdn, String title) {
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

        this.userStore = new UserStore(this);
        this.webAuthn = new WebAuthn(this, fqdn, title);
    }


}
