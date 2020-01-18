package com.ezekielnewren.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.*;

public class MemoryCredentialRepository implements CredentialRepository {

    Map<String, Set<PublicKeyCredentialDescriptor>> credStore = new HashMap<>();

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        if (credStore.containsKey(username)) return credStore.get(username);
        else return new HashSet<>();
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
}
