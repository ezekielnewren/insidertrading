package com.ezekielnewren.insidertrading.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * <p>Class that contains data about the {@code AssertionResponse}.
 * Contains the {@code Jackson} properties for serialization and deserialization.
 * Contains getters for Java objects used for assertion request.</p>
 */
@NoArgsConstructor
@Getter
public class AssertionResponse {

    /**
     * Unique requestId for the assertion.
     * @see com.yubico.webauthn.data.ByteArray
     */
    @JsonProperty @NonNull ByteArray requestId;


    /**
     * Attributes contained are returned when new assertion is requested.
     * @see com.yubico.webauthn.data.PublicKeyCredential
     * @see com.yubico.webauthn.data.AuthenticatorAssertionResponse
     * @see com.yubico.webauthn.data.ClientAssertionExtensionOutputs
     */
    @JsonProperty @NonNull PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> publicKeyCredential;

}
