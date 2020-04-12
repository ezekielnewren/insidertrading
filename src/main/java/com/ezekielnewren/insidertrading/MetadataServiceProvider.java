package com.ezekielnewren.insidertrading;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.Transport;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetadataServiceProvider implements MetadataService {

    List<MetadataService> list = new ArrayList<>();

    @Override
    public Attestation getAttestation(List<X509Certificate> attestationCertificateChain) throws CertificateEncodingException {
        for (MetadataService ser: list) {
            Attestation att = ser.getAttestation(attestationCertificateChain);
            if (att.isTrusted()) return att;
        }
        return Attestation.builder().trusted(false).build();
    }

    public void addMetadataService(MetadataService _md) {
        list.add(_md);
    }


}
