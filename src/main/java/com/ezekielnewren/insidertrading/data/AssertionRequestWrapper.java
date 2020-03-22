package com.ezekielnewren.insidertrading.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.ByteArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>Class that contains data about {@code AssertionRequest}.
 * Contains the {@code Jackson} properties for serialization and deserialization.
 * Contains getters for  used for assertion request.</p>
 */
@AllArgsConstructor
@Getter
public class AssertionRequestWrapper<T> {

    /**
     * Unique id used for request.
     * @see com.yubico.webauthn.data.ByteArray
     */
    @JsonProperty @NonNull ByteArray requestId;


    /**
     * Contains publicKey and a username.
     * @see com.yubico.webauthn.AssertionRequest
     */
    @JsonProperty @NonNull AssertionRequest assertionRequest;

    @JsonIgnore public T attachment;

}
