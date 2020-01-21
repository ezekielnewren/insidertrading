package com.ezekielnewren.webauthn.data;

import com.ezekielnewren.webauthn.AuthServletContext;
import com.ezekielnewren.webauthn.RegistrationStorage;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneOptions;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class UserStore {

    AuthServletContext ctx;
    RegistrationStorage regStore;

    public UserStore(AuthServletContext _ctx) {
        this.ctx = _ctx;
        regStore = new RegistrationStorage() {
            @Override
            public boolean addRegistrationByUsername(String username, CredentialRegistration reg) {
                User user = null;
                boolean insert = !exists(username);
                if (insert) {
                    user = new User(username, new ArrayList<>(), new ArrayList<>());
                } else {
                    user = getByUsername(username);
                }
                user.credList.add(reg);
                if (insert) {
                    ctx.getCollectionUser().insertOne(user);
                } else {
                    ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
                }
                return true;
            }

            @Override
            public Collection<CredentialRegistration> getRegistrationsByUsername(String username) {
                User user = getByUsername(username);
                if (user == null) return Collections.emptyList();
                return new ArrayList(user.credList);
            }

            @Override
            public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray credentialId) {
                User user = getByUsername(username);
                if (user == null) return Optional.empty();
                //user.credList.get(0).getCredential().getCredentialId();
                return Optional.empty();
            }

            @Override
            public Collection<CredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle) {
                ObjectId _id = new ObjectId(userHandle.getBytes());
                User user = ctx.getCollectionUser().find(Filters.eq("_id", _id)).first();
                return new ArrayList<>(user.credList);
            }

            @Override
            public boolean removeRegistrationByUsername(String username, CredentialRegistration credentialRegistration) {
                return false;
            }

            @Override
            public boolean removeAllRegistrations(String username) {
                User user = getByUsername(username);
                user.credList.clear();
                ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
                return true;
            }

            @Override
            public void updateSignatureCount(AssertionResult result) {
                String username = result.getUsername();
                ByteArray credentialId = result.getCredentialId();
                User user = getByUsername(username);
                Optional<CredentialRegistration> cred = getRegistrationByUsernameAndCredentialId(username, credentialId);
                if (cred.isPresent()) {
                    cred.get().signatureCount = result.getSignatureCount();
                    ctx.getCollectionUser().replaceOne(Filters.eq("_id", user._id), user);
                }
            }

            @Override
            public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
                return null;
            }

            @Override
            public Optional<ByteArray> getUserHandleForUsername(String username) {
                return Optional.empty();
            }

            @Override
            public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                return Optional.empty();
            }

            @Override
            public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                return Optional.empty();
            }

            @Override
            public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
                return null;
            }
        };
    }


    public RegistrationStorage getRegistrationStorage() {
        return regStore;
    }

    public User getByUsername(String username) {
        return ctx.getCollectionUser().find(Filters.eq("username", username)).first();
    }

    public User getByObjectId(ObjectId _id) {
        return ctx.getCollectionUser().find(Filters.eq("_id", _id)).first();
    }

    public boolean exists(String username) {
        return ctx.getCollectionUser().countDocuments(Filters.eq("username", username))>0;
    }

}
