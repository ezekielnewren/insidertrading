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
     * {@code Map} of usernames and unique public key credential attributes.
     * @see java.util.Map
     * @see com.yubico.webauthn.data.PublicKeyCredentialDescriptor
     */
    Map<String, Set<PublicKeyCredentialDescriptor>> credStore = new HashMap<>();

    /**
     * Checks if the username exists in the {@code HashMap}.
     * @param username the key of the {@code HashMap} and the user specified name.
     * @return the user name if one exists or a new {@code HashMap}
     * @see java.util.Set
     * @see java.lang.String
     */
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        if (credStore.containsKey(username)) return credStore.get(username);
        else return new HashSet<>();
    }

    /**
     * Get the userhandle for their username.
     * @param username users specified name.
     * @return an empty {@code Optional}
     * @see java.util.Optional
     * @see java.lang.String
     */
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return Optional.empty();
    }

    /**
     * Get username for their handle.
     * @param userHandle user handle the credential is assigned to.
     * @return an empty {@code Optional}
     * @see java.util.Optional
     * @see com.yubico.webauthn.data.ByteArray
     */
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.empty();
    }

    /**
     * Lookup up credential ids with user handle.
     * @param credentialId id of the credential.
     * @param userHandle user handle the credential is assigned to.
     * @return an empty {@code Optional}
     * @see java.util.Optional
     * @see com.yubico.webauthn.data.ByteArray
     * @see com.yubico.webauthn.RegisteredCredential
     */
    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return Optional.empty();
    }

    /**
     * Looking up all credential ids.
     * @param credentialId id of the credential.
     * @return a new {@code HashMap}
     * @see java.util.Set
     * @see com.yubico.webauthn.data.ByteArray
     */
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return new HashSet<>();
    }
}
