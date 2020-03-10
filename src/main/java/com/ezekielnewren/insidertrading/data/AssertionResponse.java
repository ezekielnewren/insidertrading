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
 *
 */
@NoArgsConstructor
@Getter
public class AssertionResponse {

    /**
     *
     */
    @JsonProperty @NonNull ByteArray requestId;


    /**
     *
     */
    @JsonProperty @NonNull PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> publicKeyCredential;

}
