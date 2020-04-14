package com.ezekielnewren.insidertrading;

import com.ezekielnewren.Build;
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
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
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

    final URL urlTOC;
    final X509Certificate root;
    final X509TrustManager tm;

    @SneakyThrows
    public MetadataServiceFido(SessionManager _ctx) {
        this.ctx = _ctx;

        urlTOC = new URL("https://mds2.fidoalliance.org/?token="+getFidoApiKey());
        CryptoManager cm = CryptoManager.getInstance();
        root = cm.getX509Certificate("fidoalliance_root");
        tm = cm.createX509TrustManager(root);
    }

    @Override
    public Attestation getAttestation(List<X509Certificate> attestationCertificateChain) throws CertificateEncodingException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @SneakyThrows
    public static String getFidoApiKey() {
        String raw = System.getenv(MetadataServiceFido.FIDO_API_KEY_PATH);
        if (raw == null) throw new RuntimeException("environment variable FIDO_API_KEY_PATH not set");
        Path pathApiKey = Paths.get(raw);
        return Files.readString(pathApiKey).substring(0, 48);
    }

    @SneakyThrows
    public JsonNode unpackJwt(String raw, ObjectMapper om) {
        // follow this for reading the jwt
        // https://fidoalliance.org/specs/fido-v2.0-id-20180227/fido-metadata-service-v2.0-id-20180227.html#metadata-toc

        Jwts.parser().setSigningKeyResolver(new SigningKeyResolver() {
            // verify certificate chain according to this rfc
            // https://tools.ietf.org/html/rfc5280

            @SneakyThrows
            public PublicKey resolveSigningKey(JwsHeader header) {
                // https://tools.ietf.org/html/rfc7515#section-4.1.6
                if (header.get("x5c") == null) return null;

                // construct certificate chain from the x5c field and add the root ca
                List<String> value = (List<String>) header.get("x5c");
                X509Certificate[] certChain = new X509Certificate[value.size()+1];
                for (int i=0; i<value.size(); i++) {
                    ByteArray der = ByteArray.fromBase64(value.get(i));
                    X509Certificate tmp = Util.createX509CertificateFromEncoded(der);
                    certChain[i] = tmp;
                }
                X509Certificate sigCert = certChain[0];
                ArrayUtils.reverse(certChain);
                certChain[0] = root;

                // let the java implementation check the certificate chain
                tm.checkClientTrusted(certChain, sigCert.getPublicKey().getAlgorithm());

                return sigCert.getPublicKey();
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
    public String downloadToc() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = urlTOC.openStream()) {
            IOUtils.copy(is, baos);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

}
