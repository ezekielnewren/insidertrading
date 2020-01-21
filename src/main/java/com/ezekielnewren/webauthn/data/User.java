package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@Getter
public class User {

    @JsonProperty @NonNull final ObjectId _id;
    @JsonProperty @NonNull String username;
    @JsonProperty @NonNull Optional<String> displayName;
    @JsonProperty @NonNull List<String> email;
    @JsonProperty @NonNull List<Authenticator> authenticator;

    @JsonCreator
    public User(@JsonProperty("_id") final ObjectId _id,
                @JsonProperty("username") String _username,
                @JsonProperty("displayName") String _displayName,
                @JsonProperty("email") List<String> _email,
                @JsonProperty("authenticator") List<Authenticator> _authenticator
    ) {
        this._id = _id;
        this.username = _username;
        this.displayName = Optional.ofNullable(_displayName);
        this.email = _email;
        this.authenticator = _authenticator;
    }

    public User(String _username, String _displayName, List<String> _email, List<Authenticator> _authenticator) {
        this(new ObjectId(), _username, _displayName, _email, _authenticator);
    }

    public ByteArray getUserHandle() {
        return new ByteArray(_id.toByteArray());
    }
    public String getDisplayName() {
        return displayName.orElse(username);
    }

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

    public Authenticator getAuthenticator(@NonNull ByteArray credentialId) {
        for (Authenticator auth: getAuthenticator()) {
            if (credentialId.equals(auth.getCredentialId())) return auth;
        }
        return null;
    }

}
