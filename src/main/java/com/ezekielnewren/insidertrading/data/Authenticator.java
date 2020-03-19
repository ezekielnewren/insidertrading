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
     * Time of registration.
     */
    @JsonProperty long registrationTime;

    /**
     * id used for assertions.
     */
    @JsonProperty ByteArray credentialId;

    /**
     * public key used to verify signatures.
     */
    @JsonProperty ByteArray publicKeyCose;

    /**
     * signature count of credential
     */
    @JsonProperty long signatureCount;

    /**
     * Container that contains {@code String}.
     * If a value is present, {@code isPresent()} returns {@code true}.
     * If no value is present, the object is considered <i>empty</i> and
     * {@code isPresent()} returns {@code false}.
     * @see java.util.Optional
     */
    @JsonProperty Optional<String> nickname;

    /**
     * Container that contains information about the authenticator device.
     * If a value is present, {@code isPresent()} returns {@code true}.
     * If no value is present, the object is considered <i>empty</i> and
     * {@code isPresent()} returns {@code false}.
     * @see java.util.Optional
     */
    @JsonProperty Optional<Attestation> attestation;

    /**
     * States what type of attestation to uses.
     * @see com.yubico.webauthn.data.AttestationType
     */
    @JsonProperty AttestationType attestationType;

    /**
     * Sets the {@code SignatureCount} on successful assertions
     * @param signatureCount
     */
    @JsonIgnore
    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

}
