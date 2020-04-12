package com.ezekielnewren.insidertrading;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.data.ByteArray;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class MetadataServiceYubico implements MetadataService {

    final SessionManager ctx;
    final static URL url;
    static {
        try {
            url = new URL("https://developers.yubico.com/U2F/yubico-metadata.json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    final JsonNode metadata;
    final X509Certificate[] trusted;

    public MetadataServiceYubico(SessionManager _ctx) {
        this.ctx = _ctx;
        ObjectMapper om = ctx.getObjectMapper();
        try {
            metadata = om.readTree(url.openStream());
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
                return Attestation.builder().trusted(true).build();
            }
        }
        return Attestation.builder().trusted(false).build();
    }
}
