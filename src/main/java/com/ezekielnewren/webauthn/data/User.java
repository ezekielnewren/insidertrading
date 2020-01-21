package com.ezekielnewren.webauthn.data;

import com.ezekielnewren.webauthn.JacksonHelper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
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

    @JsonProperty @NonNull final ObjectId _id;
    @JsonProperty @NonNull String username;
    @JsonProperty @NonNull Optional<String> displayName;
    @JsonProperty @NonNull List<String> email;
    @JsonProperty @NonNull List<Authenticator> authenticator;

    public User(String _username, String _displayName, List<String> _email, List<Authenticator> _authenticator) {
        this(new ObjectId(), _username, Optional.ofNullable(_displayName), _email, _authenticator);
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
