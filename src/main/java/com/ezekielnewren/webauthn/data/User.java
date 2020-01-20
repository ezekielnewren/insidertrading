package com.ezekielnewren.webauthn.data;

import com.ezekielnewren.webauthn.Util;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class User {

    public final ObjectId _id;
    public @NonNull String username;
    public @NonNull List<String> email = new ArrayList<>();
    public @NonNull List<CredentialRegistration> credList = new ArrayList<>();

    @JsonCreator
    User(
            @JsonProperty("_id") ObjectId _id,
            @JsonProperty("username") String _username,
            @JsonProperty("email") List<String> _email,
            @JsonProperty("credList") List<CredentialRegistration> _credList
    ) {
        this._id = _id;
        this.username = _username;
        this.email = _email!=null?_email:new ArrayList<>();
        this.credList = _credList!=null?_credList:new ArrayList<>();
    }

    public User(String _username, List<String> _email, List<CredentialRegistration> _credList) {
        this(Util.generateRandomObjectId(), _username, _email, _credList);
    }

    public ByteArray getUserHandle() {
        return new ByteArray(_id.toByteArray());
    }

}
