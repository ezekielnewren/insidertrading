package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@Getter
public class User {

    /**
     *
     */
    @JsonProperty @NonNull final ObjectId _id;

    /**
     *
     */
    @JsonProperty @NonNull String username;

    /**
     *
     */
    @JsonProperty @NonNull Optional<String> displayName;

    /**
     *
     */
    @JsonProperty @NonNull List<String> email;

    /**
     *
     */
    @JsonProperty @NonNull List<Authenticator> authenticator;

    /**
     *
     */
    @JsonProperty @NonNull String firstName;

    /**
     *
     */
    @JsonProperty @NonNull String lastName;

    /**
     *
     */
    @JsonProperty int ssn;

    /**
     *
     */
    @JsonProperty long savingAccount;

    /**
     *
     */
    @JsonProperty long checkingAccount;

    /**
     * @param _id
     * @param _username
     * @param _displayName
     * @param _email
     * @param _authenticator
     * @param _firstName
     * @param _lastName
     * @param _ssn
     * @param _savingAccount
     * @param _checkingAccount
     */
    @JsonCreator
    public User(@JsonProperty("_id") final ObjectId _id,
                @JsonProperty("username") String _username,
                @JsonProperty("displayName") String _displayName,
                @JsonProperty("email") List<String> _email,
                @JsonProperty("authenticator") List<Authenticator> _authenticator,
                @JsonProperty("firstName") String _firstName,
                @JsonProperty("lastName") String _lastName,
                @JsonProperty("ssn") int _ssn,
                @JsonProperty("savingAccount") long _savingAccount,
                @JsonProperty("checkingAccount") long _checkingAccount

    ) {
        this._id = _id;
        this.username = _username;
        this.displayName = Optional.ofNullable(_displayName);
        this.email = _email;
        this.authenticator = _authenticator;
        this.firstName = _firstName;
        this.lastName = _lastName;
        this.ssn = _ssn;
        this.savingAccount = _savingAccount;
        this.checkingAccount = _checkingAccount;
    }

    /**
     * @param _username
     * @param _displayName
     * @param _email
     * @param _authenticator
     * @param _firstName
     * @param _lastName
     * @param _ssn
     * @param _savingAccount
     * @param _checkingAccount
     */
    public User(String _username, String _displayName, List<String> _email, List<Authenticator> _authenticator, String _firstName, String _lastName, int _ssn,
                long _savingAccount, long _checkingAccount) {
        this(new ObjectId(), _username, _displayName, _email, _authenticator, _firstName, _lastName, _ssn, _savingAccount, _checkingAccount);
    }

    /**
     * @return
     */
    public User(String _username, String _displayName, ArrayList<String> _email, ArrayList<Authenticator> _authenticator) {
        this(_username, _displayName, _email, _authenticator, null, null, 0, 0, 0);
    }

    public ByteArray getUserHandle() {
        return new ByteArray(_id.toByteArray());
    }


    /**
     * @return
     */
    public String getDisplayName() {
        return displayName.orElse(username);
    }

    /**
     * @param credentialId
     * @return
     */
    public RegisteredCredential getRegisteredCredential(ByteArray credentialId) {
        Authenticator auth = getAuthenticator(credentialId);

        if (auth == null) return null;
        return RegisteredCredential.builder()
                    .credentialId(credentialId)
                    .userHandle(getUserHandle())
                    .publicKeyCose(auth.getPublicKeyCose())
                    .signatureCount(auth.getSignatureCount())
                    .build();
    }

    /**
     * @param credentialId
     * @return
     */
    public Authenticator getAuthenticator(@NonNull ByteArray credentialId) {
        for (Authenticator auth: getAuthenticator()) {
            if (credentialId.equals(auth.getCredentialId())) return auth;
        }
        return null;
    }

}
