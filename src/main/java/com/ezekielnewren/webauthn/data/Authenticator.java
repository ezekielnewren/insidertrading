package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AttestationType;
import com.yubico.webauthn.data.ByteArray;
import java.util.Optional;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Authenticator {

    long registrationTime;
    ByteArray credentialId;
    ByteArray publicKeyCose;
    long signatureCount;

    Optional<String> nickname;
    Optional<Attestation> attestation;
    AttestationType attestationType;

    @JsonIgnore
    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

}