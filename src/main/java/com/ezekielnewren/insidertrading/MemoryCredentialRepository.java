package com.ezekielnewren.insidertrading;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.*;

/**
 *
 */
public class MemoryCredentialRepository implements CredentialRepository {

    /**
     *
     * @see java.util.Map
     */
    Map<String, Set<PublicKeyCredentialDescriptor>> credStore = new HashMap<>();

    /**
     *
     * @param username
     * @return
     * @see java.util.Set
     * @see java.lang.String
     */
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        if (credStore.containsKey(username)) return credStore.get(username);
        else return new HashSet<>();
    }

    /**
     *
     * @param username
     * @return
     * @see java.util.Optional
     * @see java.lang.String
     */
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return Optional.empty();
    }

    /**
     *
     * @param userHandle
     * @return
     * @see java.util.Optional
     * @see com.yubico.webauthn.data.ByteArray
     */
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.empty();
    }

    /**
     *
     * @param credentialId
     * @param userHandle
     * @return
     * @see java.util.Optional
     * @see com.yubico.webauthn.data.ByteArray
     */
    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return Optional.empty();
    }

    /**
     *
     * @param credentialId
     * @return
     * @see java.util.Set
     * @see com.yubico.webauthn.data.ByteArray
     */
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return new HashSet<>();
    }
}
