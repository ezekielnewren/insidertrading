package com.ezekielnewren.insidertrading;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import jdk.jshell.spi.ExecutionControl;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

public class MetadataServiceFido implements MetadataService {

    final SessionManager ctx;
    public static final String FIDO_API_KEY_PATH = "FIDO_API_KEY_PATH";

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


}
