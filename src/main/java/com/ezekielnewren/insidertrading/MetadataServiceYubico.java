package com.ezekielnewren.insidertrading;

import com.ezekielnewren.Build;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.exception.HexException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MetadataServiceYubico implements MetadataService {
    // format explanation
    // https://developers.yubico.com/U2F/Attestation_and_Metadata/JSON_Format.html
    // additional aaguid
    // https://support.yubico.com/support/solutions/articles/15000028710-yubikey-hardware-fido2-aaguids
    // fido alliance metadata
    // https://fidoalliance.org/specs/fido-uaf-v1.1-id-20170202/fido-metadata-statement-v1.1-id-20170202.html#metadata-statement-format

    final SessionManager ctx;
    final JsonNode metadata;
    final X509Certificate[] trusted;

    public MetadataServiceYubico(SessionManager _ctx) {
        this.ctx = _ctx;
        ObjectMapper om = ctx.getObjectMapper();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("yubico-metadata.json")) {
            metadata = om.readTree(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonNode array = metadata.get("trustedCertificates");
        trusted = new X509Certificate[array.size()];
        for (int i=0; i<array.size(); i++) {
            String pem = array.get(i).asText();
            ByteArray binary = new ByteArray(pem.getBytes(StandardCharsets.UTF_8));
            trusted[i] = Util.createX509CertificateFromPem(binary);
        }
    }

    @Override
    public Attestation getAttestation(List<X509Certificate> attestationCertificateChain) throws CertificateEncodingException {
        if (attestationCertificateChain.size() > 0) {
            X509Certificate cur = attestationCertificateChain.get(0);
            X509Certificate rootCert = null;
            for (X509Certificate root: trusted) {
                if (Util.verify(cur, root)) {
                    rootCert = root;
                    break;
                }
            }
            if (rootCert != null) {
                for (int i=1; i<attestationCertificateChain.size(); i++) {
                    X509Certificate next = attestationCertificateChain.get(i);
                    if (!Util.verify(next, cur)) break;
                    cur = next;
                }
                return Attestation.builder()
                        .trusted(true)
                        .metadataIdentifier((String) null)
                        .vendorProperties((Map<String, String>) null)
                        .deviceProperties((Map<String, String>) null)
                        .build();
            }
        }


        return Attestation.builder().trusted(false).build();
    }

    public Optional<JsonNode> get(String deviceId, Optional<ByteArray> aaguid) {
        for (JsonNode node: metadata.get("devices")) {
            if (aaguid.isPresent()) {
                for (JsonNode sel: node.get("selectors")) {
                    JsonNode tmp0 = sel.get("parameters").get("value");
                    if (tmp0 == null) continue;
                    JsonNode tmp = tmp0.get("value");
                    if (tmp == null) continue;
                    ByteArray a = aaguid.orElseThrow();
                    ByteArray b;
                    try {b = ByteArray.fromHex(tmp.asText());} catch (HexException e) {continue;}
                    if (a.equals(b)) {
                        return Optional.of(node);
                    }
                }
            } else {
                if (node.get("deviceId").asText().equals(deviceId)) {
                    return Optional.of(node);
                }
            }
        }
        return Optional.empty();
    }



}
