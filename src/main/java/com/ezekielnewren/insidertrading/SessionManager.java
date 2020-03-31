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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

/**
 * Class contains a set of methods used to communicate with the servlet container e.g. {@code HTTP}.
 */
@Value
@Getter
public class SessionManager {

    /**
     * Creating a new constant object.
     * @see java.lang
     */
    final Object mutex = new Object();

    Map<String, String> sessionIdAndUsername = new HashMap<>();
    Map<String, HttpSession> sessionIdAndHttpSession = new HashMap<>();

    /**
     * Object for {@code ObjectMapper}.
     * @see com.fasterxml.jackson.databind.ObjectMapper
     */
    ObjectMapper objectMapper;

    /**
     * Object for a {@code Mongo Client}
     * @see com.mongodb.client.MongoClient
     */
    MongoClient client;

    /**
     * Object for mongo database
     * @see com.mongodb.client.MongoDatabase
     */
    MongoDatabase database;


    /**
     * Collection of {@code User}s.
     *
     * <p>
     *     MongoCollections are generic
     * </p>
     *
     * @see com.mongodb.client.MongoCollection
     */
    public MongoCollection<User> collectionUser;

    /**
     * Collection of {@code Document}s.
     *
     * <p>
     *     MongoCollections are generic
     * </p>
     *
     * @see com.mongodb.client.MongoCollection
     */
    public MongoCollection<Document> collectionData;


    /**
     * Collection of {@code Transactions}.
     *
     * <p>
     *     MongoCollections are generic
     * </p>
     *
     * @see com.mongodb.client.MongoCollection
     */
    public MongoCollection<Transaction> collectionTransaction;


    /**
     * Object for {@code WebAuthn}.
     *
     * <p>
     *     WebAuthn handles client and server authentication.
     * </p>
     *
     * @see com.ezekielnewren.insidertrading.WebAuthn
     */
    WebAuthn webAuthn;

    /**
     * Object for {@code UserStore}.
     *
     * <p>
     *     {@code UserStore} stores user information in database.
     * </p>
     *
     * @see com.ezekielnewren.insidertrading.data.UserStore
     */
    UserStore userStore;

    BankAPI api;

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
    public SessionManager(ObjectMapper _om, String connectionString, MongoCredential _cred, String fqdn, String title) {
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

        this.api = new BankAPI(this);
    }

    public boolean isLoggedIn(HttpSession httpSession) { return isLoggedIn(httpSession, null); }
    public boolean isLoggedIn(HttpSession httpSession, String username) {
        Objects.nonNull(httpSession);

        String tmp = sessionIdAndUsername.get(httpSession.getId());
        if (username == null && tmp != null) return true;
        if (username == null && tmp == null) return false;
        if (username.equals(tmp)) return true;
        return false;
    }


    public void setLoggedIn(HttpSession httpSession, String username) {
        Objects.nonNull(httpSession);
        Objects.nonNull(username);

        sessionIdAndUsername.put(httpSession.getId(), username);
        sessionIdAndHttpSession.put(httpSession.getId(), httpSession);
    }

    public void clearLoggedIn(HttpSession httpSession) {
        sessionIdAndUsername.put(httpSession.getId(), null);
        sessionIdAndHttpSession.put(httpSession.getId(), null);
    }

    public String getUsername(HttpSession httpSession) {
        return sessionIdAndUsername.get(httpSession.getId());
    }

    public List<HttpSession> getEverySession(String _username) {
        Objects.nonNull(_username);

        List<HttpSession> tmp = new ArrayList<>();

        sessionIdAndHttpSession.forEach((session, username)->{
            if (_username.equals(username)) {
                tmp.add(sessionIdAndHttpSession.get(session));
            }
        });

        return tmp;
    }

}
