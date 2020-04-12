package com.ezekielnewren.insidertrading;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.Transport;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

public class MetadataServiceProvider implements MetadataService {

    Map<Class<? extends MetadataService>, MetadataService> list = new LinkedHashMap<>();

    @Override
    public Attestation getAttestation(List<X509Certificate> attestationCertificateChain) throws CertificateEncodingException {
        for (MetadataService ser: list.values()) {
            Attestation att = ser.getAttestation(attestationCertificateChain);
            if (att.isTrusted()) return att;
        }
        return Attestation.builder().trusted(false).build();
    }

    public void addMetadataService(MetadataService _md) {
        list.put(_md.getClass(), _md);
    }

    public <T extends MetadataService> T get(Class<T> cls) {
        return (T) list.get(cls);
    }

}
