package com.ezekielnewren.insidertrading.data;

import com.ezekielnewren.insidertrading.InsiderTradingServletContext;
import com.mongodb.client.model.Filters;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Class contains methods used to store user information to database.
 */
public class UserStore {

    /**
     * Variable for servlet.
     */
    InsiderTradingServletContext ctx;


    /**
     * Variable for repository, used to look up credentials.
     *
     * <p>
     * Used to look up credentials, usernames and user handles from usernames, user handles
     * and credential IDs.
     * </p>
     */
    CredentialRepository repo;

    /**
     * Method for storing user data.
     * @param _ctx servlet context.
     */
    public UserStore(InsiderTradingServletContext _ctx) {
        this.ctx = _ctx;
        repo = new CredentialRepository() {

            /**
             *
             * @param username
             * @return
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
             *
             * @param username
             * @return
             */
            @Override
            public Optional<ByteArray> getUserHandleForUsername(String username) {
                User user = getByUsername(username);
                if (user == null) return Optional.empty();

                return Optional.of(user.getUserHandle());
            }

            /**
             *
             * @param userHandle
             * @return
             */
            @Override
            public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                User user = getByUserHandle(userHandle);
                if (user == null) return Optional.empty();

                return Optional.of(user.getUsername());
            }

            /**
             *
             * @param credentialId
             * @param userHandle
             * @return
             */
            @Override
            public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                User user = getByUserHandle(userHandle);
                if (user == null) return Optional.empty();

                return Optional.ofNullable(user.getRegisteredCredential(credentialId));
            }

            /**
             *
             * @param credentialId
             * @return
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
     * @param username
     * @param displayName
     * @param auth
     */
    public void addAuthenticator(String username, String displayName, Authenticator auth, String firstName, String lastName, int ssn, long savingAccount, long checkingAccount) {
        User user = null;
        boolean insert = !exists(username);
        if (insert) {
            user = new User(username, displayName, new ArrayList<String>(), new ArrayList<Authenticator>(), firstName, lastName, ssn, new ArrayList<Account>());
        } else {
            user = getByUsername(username);
        }
        user.authenticator.add(auth);
        if (insert) {
            ctx.getCollectionUser().insertOne(user);
        } else {
            ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
        }
    }

    /**
     *
     * @param result
     */
    public void updateSignatureCount(AssertionResult result) {
        String username = result.getUsername();
        ByteArray credentialId = result.getCredentialId();

        User user = getByUsername(username);
        if (user == null) throw new RuntimeException("could not find user associated with the AssertionResult");

        Authenticator auth = user.getAuthenticator(credentialId);

        if (auth != null) {
            auth.setSignatureCount(result.getSignatureCount());
            ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
        }
    }

    /**
     * Gets the credential repository.
     * @return returns the repository.
     */
    public CredentialRepository getCredentialRepository() {
        return repo;
    }

    /**
     * Gets the username from database via the servlet.
     * @param username the username.
     * @return the first user name that matches.
     */
    public User getByUsername(String username) {
        return ctx.getCollectionUser().find(Filters.eq("username", username)).first();
    }

    /**
     * Gets the user handle.
     * @param userHandle user handle.
     * @return the first id that matches.
     */
    public User getByUserHandle(ByteArray userHandle) {
        return getByObjectId(new ObjectId(userHandle.getBytes()));
    }

    /**
     * Gets object Id.
     * @param _id 12-byte primary key value for user.
     * @return the first id that matches.
     */
    public User getByObjectId(ObjectId _id) {
        return ctx.getCollectionUser().find(Filters.eq("_id", _id)).first();
    }

    /**
     * Gets all users using {@code Iterator} until all items are consumed.
     * @return all users.
     */
    public Iterable<User> getAll() {
        return ctx.getCollectionUser().find();
    }

    /////needs revising

    /**
     * Checks if username exists.
     * @param username the username.
     * @return true or false
     */
    public boolean exists(String username) {
        return ctx.getCollectionUser().countDocuments(Filters.eq("username", username))>0;
    }

}
