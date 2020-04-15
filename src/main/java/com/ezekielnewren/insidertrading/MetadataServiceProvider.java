package com.ezekielnewren.insidertrading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.Transport;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

public class MetadataServiceProvider implements MetadataService {

    Map<Class<? extends MetadataService>, MetadataService> list = new LinkedHashMap<>();
    ObjectMapper om = JacksonHelper.newObjectMapper();

    @Override
    public Attestation getAttestation(List<X509Certificate> attestationCertificateChain) throws CertificateEncodingException {
        Attestation attest = null;
        for (MetadataService ser: list.values()) {
            Attestation att = ser.getAttestation(attestationCertificateChain);
            if (att.isTrusted()) {
                attest = att;
                break;
            }
        }



        Attestation.AttestationBuilder build = Attestation.builder().trusted(attest != null);

        // device properties
        Map<String, String> deviceProperties;
        if (attest != null && attest.getDeviceProperties().isPresent()) deviceProperties = attest.getDeviceProperties().orElseThrow();
        else deviceProperties = new LinkedHashMap<>();

        ArrayNode certificateListAsJsonPemArray = om.createArrayNode();
        for (X509Certificate item: attestationCertificateChain) {
            String value = Util.createPemFromX509Certificate(item);
            certificateListAsJsonPemArray.add(value);
        }
        deviceProperties.put("certificateChain", certificateListAsJsonPemArray.toString());
        build.deviceProperties(deviceProperties);

        if (attest != null) {
            build.metadataIdentifier(attest.getMetadataIdentifier());
            build.vendorProperties(attest.getVendorProperties());
            build.transports(attest.getTransports());
        }

        return build.build();
    }

    public void addMetadataService(MetadataService _md) {
        list.put(_md.getClass(), _md);
    }

    public <T extends MetadataService> T get(Class<T> cls) {
        return (T) list.get(cls);
    }

}
