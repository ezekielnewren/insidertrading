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
 * Contains information about the {@code AssertionResponse}.
 * Data is given assigned in {@link com.ezekielnewren.insidertrading.InsiderTradingServlet}s {@code doPost} method.
 */
@NoArgsConstructor
@Getter
public class AssertionResponse {

    /**
     * The requestId for the assertion.
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
