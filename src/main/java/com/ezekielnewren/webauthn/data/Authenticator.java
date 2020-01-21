package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AttestationType;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.UserIdentity;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(onConstructor_={@JsonCreator})
@Getter
public class Authenticator {

    final long registrationTime;
    final ByteArray publicKeyCose;
    long signatureCount;

    Optional<String> nickname;
    Optional<Attestation> attestation;
    AttestationType attestationType;

    @JsonIgnore
    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

}
