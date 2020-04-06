package com.ezekielnewren.insidertrading.data;

import com.ezekielnewren.insidertrading.JacksonHelper;
import com.ezekielnewren.insidertrading.SessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Class contains methods used to store user information to database.
 */
public class UserStore {

    /**
     * Object for servlet.
     * @see SessionManager
     */
    SessionManager ctx;


    /**
     * Object for repository, used to look up credentials.
     *
     * <p>
     * Used to look up credentials, usernames and user handles from usernames, user handles
     * and credential IDs.
     * </p>
     *
     * @see com.yubico.webauthn.CredentialRepository
     */
    CredentialRepository repo;

    /**
     * Method for storing user data.
     * @param _ctx servlet context.
     * @see SessionManager
     */
    public UserStore(SessionManager _ctx) {
        this.ctx = _ctx;
        repo = new CredentialRepository() {

            /**
             * <p>Creates a unique collection, then adds and id for each item of the
             * {@link com.ezekielnewren.insidertrading.data.Authenticator} to it.</p>
             * @param username specified name.
             * @return the unique collection.
             * @see java.util.Set
             * @see com.yubico.webauthn.data.PublicKeyCredential
             */
            @Override
            public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
                User user = getByUsername(username);
                if (user == null) return new HashSet<>();

                Set<PublicKeyCredentialDescriptor> tmp = new HashSet<>();
                user.getAuthenticator().forEach((item)->{
                    tmp.add(PublicKeyCredentialDescriptor.builder().id(item.getCredentialId()).build());
                });
                return tmp;
            }

            /**
             * Gets the users handle.
             * @param username specified name.
             * @return {@code Optional} with value.
             * @see java.util.Optional
             * @see java.lang.String
             */
            @Override
            public Optional<ByteArray> getUserHandleForUsername(String username) {
                User user = getByUsername(username);
                if (user == null) return Optional.empty();

                return Optional.of(user.getUserHandle());
            }

            /**
             * Gets username.
             * @param userHandle specified handle.
             * @return {@code Optional} with value.
             * @see java.util.Optional
             * @see com.yubico.webauthn.data.ByteArray
             */
            @Override
            public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                User user = getByUserHandle(userHandle);
                if (user == null) return Optional.empty();

                return Optional.of(user.getUsername());
            }

            /**
             * Used to lookup users.
             * @param credentialId id used for assertion.
             * @param userHandle user handle.
             * @return {@code Optional} returns with value, if null returns {@code empty Optional}.
             * @see java.util.Optional
             * @see com.yubico.webauthn.data.ByteArray
             */
            @Override
            public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                User user = getByUserHandle(userHandle);
                if (user == null) return Optional.empty();

                return Optional.ofNullable(user.getRegisteredCredential(credentialId));
            }

            /**
             * <p>Creates a unique collection, then adds all registered users in
             * {@link com.yubico.webauthn.RegisteredCredential} to it.</p>
             * @param credentialId id used for assertion.
             * @return the unique collection.
             * @see java.util.Set
             * @see com.yubico.webauthn.RegisteredCredential
             * @see com.yubico.webauthn.data.ByteArray
             */
            @Override
            public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
                Set<RegisteredCredential> tmp = new HashSet<>();
                for (User user: getAll()) {
                    RegisteredCredential rc = user.getRegisteredCredential(credentialId);
                    if (rc != null) tmp.add(rc);
                }
                return tmp;
            }
        };
    }


    /**
     * Adds an {@code Authenticator} if user is {@code null}.
     * Adds user information to the {@code MongoDB}.
     * @param username user name.
     * @param displayName display name.
     * @param auth Authenticator.
     * @param firstName user first name.
     * @param lastName user last name.
     * @param ssn social security
     * @param savingAccount savings account
     * @param checkingAccount checking account
     * @see java.lang.String
     * @see com.ezekielnewren.insidertrading.data.Authenticator
     */
    public void addAuthenticator(String username, String displayName, Authenticator auth, String firstName, String lastName, int ssn, long savingAccount, long checkingAccount) {
        User user = null;
        boolean insert = !exists(username);
        if (insert) {
            user = createUser(username, displayName);
        } else {
            user = getByUsername(username);
        }
        user.authenticator.add(auth);
        writeToDatabase(user);
    }

    /**
     * <p>Checks if user exist in the database, if it doesn't creates the {@code User} object
     * and writes it to the database.</p>
     * @param username the {@code username}.
     * @param displayName optional name, {@code username} if none is specified.
     * @return a new {@code User} object.
     */
    public User createUser(String username, String displayName) {
        if (!exists(username)) {
            User user = new User(username, displayName);
            writeToDatabase(user);
            return user;
        }
        throw new RuntimeException("cannot create user "+username+"");
    }

    //may be incorrect
    /**
     * Updates on each successful authenticator assertion.
     * @param result contains information from steps of mandatory assertions.
     * @see com.yubico.webauthn.AssertionResult
     */
    public void updateSignatureCount(AssertionResult result) {
        String username = result.getUsername();
        ByteArray credentialId = result.getCredentialId();

        User user = getByUsername(username);
        if (user == null) throw new RuntimeException("could not find user associated with the AssertionResult");

        Authenticator auth = user.getAuthenticator(credentialId);

        if (auth != null) {
            auth.setSignatureCount(result.getSignatureCount());
            writeToDatabase(user);
        }
    }

    /**
     * Gets the credential repository.
     * @return returns the repository.
     * @see com.yubico.webauthn.CredentialRepository
     */
    public CredentialRepository getCredentialRepository() {
        return repo;
    }

    /**
     * Gets the username from database via the servlet.
     * @param username the username.
     * @return the first user name that matches.
     * @see com.ezekielnewren.insidertrading.data.User
     * @see java.lang.String
     */
    public User getByUsername(String username) {
        return ctx.getCollectionUser().find(Filters.eq("username", username)).first();
    }

    /**
     * Gets the user handle.
     * @param userHandle user handle.
     * @return the first id that matches.
     * @see com.ezekielnewren.insidertrading.data.User
     * @see com.yubico.webauthn.data.ByteArray
     */
    public User getByUserHandle(ByteArray userHandle) {
        return getByObjectId(new ObjectId(userHandle.getBytes()));
    }

    /**
     * Gets object Id.
     * @param _id 12-byte primary key value for user.
     * @return the first id that matches.
     * @see com.ezekielnewren.insidertrading.data.User
     * @see org.bson.types.ObjectId
     */
    public User getByObjectId(ObjectId _id) {
        return ctx.getCollectionUser().find(Filters.eq("_id", _id)).first();
    }

    /**
     * Gets all users using {@code Iterator} until all items are consumed.
     * @return all users.
     * @see java.lang.Iterable
     */
    public Iterable<User> getAll() {
        return ctx.getCollectionUser().find();
    }

    /////needs revising

    /**
     * Checks if username exists.
     * @param username the username.
     * @return true or false.
     * @see java.lang.String
     */
    public boolean exists(String username) {
        return ctx.getCollectionUser().countDocuments(Filters.eq("username", username))>0;
    }


    /**
     * Writes the user information to the database.
     * @param user user information.
     * @see com.ezekielnewren.insidertrading.data.User
     */
    public void writeToDatabase(User user) {
        ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user, new ReplaceOptions().upsert(true));
    }
}
