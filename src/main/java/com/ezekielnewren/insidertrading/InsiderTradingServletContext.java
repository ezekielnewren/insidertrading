package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.JacksonCodecProvider;
import com.ezekielnewren.insidertrading.data.Transaction;
import com.ezekielnewren.insidertrading.data.User;
import com.ezekielnewren.insidertrading.data.UserStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import lombok.Getter;
import lombok.Value;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Class contains a set of methods used to communicate with the servlet container e.g. HTTP.
 */
@Value
@Getter
public class InsiderTradingServletContext {

    /**
     * Creating a new constant object.
     * @see java.lang
     */
    final Object mutex = new Object();

    /**
     * Creating an objectMapper variable
     *
     * @see com.fasterxml.jackson.databind.ObjectMapper
     */
    ObjectMapper objectMapper;

    /**
     * Variable for a mongo client
     * @see com.mongodb.client.MongoClient
     */
    MongoClient client;

    /**
     * Variable for mongo database
     * @see com.mongodb.client.MongoDatabase
     */
    MongoDatabase database;


    /**
     * Declare a collection of users.
     *
     * <p>
     *     MongoCollections are generic
     * </p>
     *
     * @see com.mongodb.client.MongoCollection
     */
    public MongoCollection<User> collectionUser;

    /**
     * Declare a collection of documents.
     *
     * <p>
     *     MongoCollections are generic
     * </p>
     *
     * @see com.mongodb.client.MongoCollection
     */
    public MongoCollection<Document> collectionData;


    /**
     * Declare a collection of transactions.
     *
     * <p>
     *     MongoCollections are generic
     * </p>
     *
     * @see com.mongodb.client.MongoCollection
     */
    public MongoCollection<Transaction> collectionTransaction;


    /**
     * Variable for WebAuthn.
     *
     * <p>
     *     WebAuthn handles client and server authentication.
     * </p>
     *
     * @see com.ezekielnewren.insidertrading.WebAuthn
     */
    WebAuthn webAuthn;

    /**
     * Variable for UserStore.
     *
     * <p>
     *     UserStore stores user information in database.
     * </p>
     *
     * @see com.ezekielnewren.insidertrading.data.UserStore
     */
    UserStore userStore;

    /**
     * Method creates the connection to the {@code Mongo Server}
     * @param _om object mapper from {@link com.ezekielnewren.insidertrading.JacksonHelper}
     * @param connectionString details of how to connect to {@code MongoDB}
     * @param _cred credentials for the {@code Mongo Server}.
     * @param fqdn fully qualified domain name.
     * @param title account type.
     * @see com.fasterxml.jackson.databind.ObjectMapper
     * @see java.lang.String
     * @see com.mongodb.MongoCredential
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
        this.collectionTransaction = database.getCollection("transaction", Transaction.class);

        this.userStore = new UserStore(this);
        this.webAuthn = new WebAuthn(this, fqdn, title);

        // apply a unique constraint on user.username and transaction.number
        collectionUser.createIndex(new BasicDBObject("username", 1), new IndexOptions().unique(true));
        // collectionTransaction.createIndex(new BasicDBObject("number", 1), new IndexOptions().unique(true));
    }


}
