package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AttestationType;
import com.yubico.webauthn.data.ByteArray;
import java.util.Optional;

import lombok.*;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Authenticator {

    /**
     *
     */
    @JsonProperty long registrationTime;

    /**
     *
     */
    @JsonProperty ByteArray credentialId;

    /**
     *
     */
    @JsonProperty ByteArray publicKeyCose;

    /**
     *
     */
    @JsonProperty long signatureCount;

    /**
     *
     */
    @JsonProperty Optional<String> nickname;

    /**
     *
     */
    @JsonProperty Optional<Attestation> attestation;

    /**
     *
     */
    @JsonProperty AttestationType attestationType;

    /**
     * @param signatureCount
     */
    @JsonIgnore
    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

}
