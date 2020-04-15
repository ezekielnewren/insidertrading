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

    // https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-authenticator-transports-extension-v1.2-ps-20170411.html#fido-u2f-extensions
    public static final String OID_ID_FIDO = "1.3.6.1.4.1.45724";
    public static final String OID_ID_YUBICO = "1.3.6.1.4.1.41482";


    /**
     * X509 Certificate extension OID for id-fido-u2f-ce-transports
     * @see <a href="https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-authenticator-transports-extension-v1.2-ps-20170411.html#fido-u2f-extensions">id-fido-u2f-ce-transports</a>
     */
    public static final String OID_ID_FIDO_U2F_CE_TRANSPORTS = OID_ID_FIDO+".2.1.1";

    /**
     * X509 Certificate extension OID for id-fido-gen-ce-aaguid
     * @see <a href="https://www.w3.org/TR/webauthn/#packed-attestation-cert-requirements">id-fido-gen-ce-aaguid</a>
     */
    public static final String OID_ID_FIDO_GEN_CE_AAGUID = OID_ID_FIDO+".1.1.4";

    /**
     * This seems to be the class identification.
     * @see <a href="https://oidref.com/1.3.6.1.4.1.41482">oid of yubico</a>
     *
     */
    public static final String OID_DEVICE_ID = OID_ID_YUBICO+".2";

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
