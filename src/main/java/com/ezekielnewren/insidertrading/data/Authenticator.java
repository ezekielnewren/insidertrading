package com.ezekielnewren.insidertrading.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AttestationType;
import com.yubico.webauthn.data.ByteArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * Class that can "obtain" authentication information from the connection.
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
     * sets the SignatureCount
     * @param signatureCount
     */
    @JsonIgnore
    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

}
