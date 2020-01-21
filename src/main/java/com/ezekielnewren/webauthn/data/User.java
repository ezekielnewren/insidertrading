package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@AllArgsConstructor(onConstructor_={@JsonCreator})
@Getter
public class User {

    final ObjectId _id;
    @NonNull String username;
    @NonNull Optional<String> displayName;
    @NonNull List<String> email;
    @NonNull Map<ByteArray, Authenticator> authenticator;

    public User(String _username, String _displayName, List<String> _email, Map<ByteArray, Authenticator> _authenticator) {
        this(new ObjectId(), _username, Optional.ofNullable(_displayName), _email, _authenticator);
    }

    public ByteArray getUserHandle() {
        return new ByteArray(_id.toByteArray());
    }
    public String getDisplayName() {
        return displayName.orElse(username);
    }

    public RegisteredCredential getRegisteredCredential(ByteArray credentialId) {
        Authenticator auth = getAuthenticator().get(credentialId);
        if (auth == null) return null;
        return RegisteredCredential.builder()
                    .credentialId(credentialId)
                    .userHandle(getUserHandle())
                    .publicKeyCose(auth.getPublicKeyCose())
                    .signatureCount(auth.getSignatureCount())
                    .build();
    }

}
