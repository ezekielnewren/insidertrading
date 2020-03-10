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
 * The class User contains user information, constructs user JSON and user object.
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
     * 12-byte primary key value for user
     */
    @JsonProperty @NonNull final ObjectId _id;

    /**
     * user name for user
     */
    @JsonProperty @NonNull String username;

    /**
     * display name for user
     */
    @JsonProperty @NonNull Optional<String> displayName;

    /**
     * list of emails for user
     */
    @JsonProperty @NonNull List<String> email;

    /**
     * list of authenticators for user
     */
    @JsonProperty @NonNull List<Authenticator> authenticator;

    /**
     * first name for user
     */
    @JsonProperty @NonNull String firstName;

    /**
     * last name for user
     */
    @JsonProperty @NonNull String lastName;

    /**
     * ssn for user
     */
    @JsonProperty int ssn;

    /**
     *Accounts for user
     */
    @JsonProperty List<Account> accounts;


    /**
     * Constructs a User JSON object.
     * @param _id generated user id.
     * @param _username user specified name.
     * @param _displayName user specified name (optional).
     * @param _email list user specified email(s).
     * @param _authenticator list user give authenticator(s).
     * @param _firstName user specified firstname.
     * @param _lastName user specified lastname.
     * @param _ssn user specified ssn.
     * @param _accounts used for Accounts.
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
                @JsonProperty("accounts") List<Account> _accounts

    ) {
        this._id = _id;
        this.username = _username;
        this.displayName = Optional.ofNullable(_displayName);
        this.email = _email;
        this.authenticator = _authenticator;
        this.firstName = _firstName;
        this.lastName = _lastName;
        this.ssn = _ssn;
        this.accounts = _accounts;
    }

    /**
     * Constructs a user object.
     * @param _username user specified name.
     * @param _displayName user specified name (optional).
     * @param _email list of user specified email(s).
     * @param _authenticator list of authenticator(s).
     * @param _firstName user specified firstname.
     * @param _lastName user specified lastame.
     * @param _ssn user specified ssn.
     * @param _savingAccount user specified savings account.
     *      * @param _checkingAccount user specified checking account.
     */
    public User(String _username, String _displayName, List<String> _email, List<Authenticator> _authenticator, String _firstName, String _lastName, int _ssn,
                List<Account> _accounts) {
        this(new ObjectId(), _username, _displayName, _email, _authenticator, _firstName, _lastName, _ssn, _accounts);
    }

    //possible duplicate?
    /**
     * Constructs a user object.
     * @param _username user specified name.
     * @param _displayName user specified name (optional).
     * @param _email list of user specified email(s).
     * @param _authenticator list of user specified authenticator(s).
     */
    public User(String _username, String _displayName, ArrayList<String> _email, ArrayList<Authenticator> _authenticator) {
        this(_username, _displayName, _email, _authenticator, null, null, 0, null);
    }

    /**
     * Used to handler, mainly when optional,
     * @return new bytearray with id bytearray.
     */
    public ByteArray getUserHandle() {
        return new ByteArray(_id.toByteArray());
    }


    /**
     *
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
     * @return null
     */
    public Authenticator getAuthenticator(@NonNull ByteArray credentialId) {
        for (Authenticator auth: getAuthenticator()) {
            if (credentialId.equals(auth.getCredentialId())) return auth;
        }
        return null;
    }

}
