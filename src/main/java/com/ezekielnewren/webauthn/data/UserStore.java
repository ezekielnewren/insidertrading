package com.ezekielnewren.webauthn.data;

import com.ezekielnewren.webauthn.AuthServletContext;
import com.mongodb.client.model.Filters;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.bson.types.ObjectId;

import java.util.*;

public class UserStore {

    AuthServletContext ctx;
    CredentialRepository repo;

    public UserStore(AuthServletContext _ctx) {
        this.ctx = _ctx;
        repo = new CredentialRepository() {
            @Override
            public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
                User user = getByUsername(username);
                if (user == null) return new HashSet<>();

                Set<PublicKeyCredentialDescriptor> tmp = new HashSet<>();
                user.getAuthenticator().keySet().forEach((item)->{
                    tmp.add(PublicKeyCredentialDescriptor.builder().id(item).build());
                });
                return tmp;
            }

            @Override
            public Optional<ByteArray> getUserHandleForUsername(String username) {
                User user = getByUsername(username);
                if (user == null) return Optional.empty();

                return Optional.of(user.getUserHandle());
            }

            @Override
            public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                User user = getByUserHandle(userHandle);
                if (user == null) return Optional.empty();

                return Optional.of(user.getUsername());
            }

            @Override
            public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                User user = getByUserHandle(userHandle);
                if (user == null) return Optional.empty();

                return Optional.ofNullable(user.getRegisteredCredential(credentialId));
            }

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

    public void addAuthenticator(String username, String displayName, ByteArray credentialId, Authenticator auth) {
        User user = null;
        boolean insert = !exists(username);
        if (insert) {
            user = new User(username, displayName, new ArrayList<>(), new HashMap<>());
        } else {
            user = getByUsername(username);
        }
        user.authenticator.put(credentialId, auth);
        if (insert) {
            ctx.getCollectionUser().insertOne(user);
        } else {
            ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
        }
    }

    public void updateSignatureCount(AssertionResult result) {
        String username = result.getUsername();
        ByteArray credentialId = result.getCredentialId();

        User user = getByUsername(username);
        if (user == null) throw new RuntimeException("could not find user associated with the AssertionResult");

        Authenticator auth = user.getAuthenticator().get(credentialId);

        if (auth != null) {
            auth.setSignatureCount(result.getSignatureCount());
            ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
        }
    }

    public CredentialRepository getCredentialRepository() {
        return repo;
    }

    public User getByUsername(String username) {
        return ctx.getCollectionUser().find(Filters.eq("username", username)).first();
    }

    public User getByUserHandle(ByteArray userHandle) {
        return getByObjectId(new ObjectId(userHandle.getBytes()));
    }

    public User getByObjectId(ObjectId _id) {
        return ctx.getCollectionUser().find(Filters.eq("_id", _id)).first();
    }

    public Iterable<User> getAll() {
        return ctx.getCollectionUser().find();
    }

    public boolean exists(String username) {
        return ctx.getCollectionUser().countDocuments(Filters.eq("username", username))>0;
    }

}
