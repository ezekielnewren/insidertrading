package com.ezekielnewren.insidertrading;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.data.ByteArray;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import jdk.jshell.spi.ExecutionControl;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetadataServiceFido implements MetadataService {

    final SessionManager ctx;
    public static final String FIDO_API_KEY_PATH = "FIDO_API_KEY_PATH";

    static final URL urlTOC;
    static {
        try {
            urlTOC = new URL("https://mds2.fidoalliance.org/?token="+getFidoApiKey());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    public MetadataServiceFido(SessionManager _ctx) {
        this.ctx = _ctx;

    }

    @Override
    public Attestation getAttestation(List<X509Certificate> attestationCertificateChain) throws CertificateEncodingException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @SneakyThrows
    public static String getFidoApiKey() {
        String raw = System.getenv(MetadataServiceFido.FIDO_API_KEY_PATH);
        Path pathApiKey = Paths.get(raw);
        return Files.readString(pathApiKey).substring(0, 48);
    }

    @SneakyThrows
    public static JsonNode unpackJwt(String raw, X509Certificate root, ObjectMapper om) {
        // follow this for reading the jwt
        // https://fidoalliance.org/specs/fido-v2.0-id-20180227/fido-metadata-service-v2.0-id-20180227.html#metadata-toc

        Jwts.parser().setSigningKeyResolver(new SigningKeyResolver() {
            // verify certificate chain according to this rfc
            // https://tools.ietf.org/html/rfc5280

            @SneakyThrows
            public PublicKey resolveSigningKey(JwsHeader header) {
                // https://tools.ietf.org/html/rfc7515#section-4.1.6
                if (header.get("x5c") == null) return null;

                List<X509Certificate> certChain = new ArrayList<>();
                for (String item: (List<String>) header.get("x5c")) {
                    ByteArray der = ByteArray.fromBase64(item);
                    X509Certificate tmp = Util.createX509CertificateFromEncoded(der);
                    certChain.add(tmp);
                }
                X509Certificate signingCert = certChain.get(0);
                Collections.reverse(certChain);

                X509Certificate prev = root;
                for (X509Certificate cur: certChain) {
                    if (!Util.verify(cur, prev)) {
                        throw new CertificateException("Invalid certificate chain");
                    }
                    prev = cur;
                }

                return signingCert.getPublicKey();
            }

            @Override
            public Key resolveSigningKey(JwsHeader header, Claims claims) {
                return resolveSigningKey(header);
            }

            @Override
            public Key resolveSigningKey(JwsHeader header, String plaintext) {
                return resolveSigningKey(header);
            }
        }).parse(raw);

        String[] section = raw.split("\\.");
        ObjectNode json = om.createObjectNode();

        json.set("header", om.readTree(new String(ByteArray.fromBase64Url(section[0]).getBytes(), StandardCharsets.UTF_8)));
        json.set("body", om.readTree(new String(ByteArray.fromBase64Url(section[1]).getBytes(), StandardCharsets.UTF_8)));

        return json;
    }

    @SneakyThrows
    public static String downloadToc() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = urlTOC.openStream()) {
            IOUtils.copy(is, baos);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

}
