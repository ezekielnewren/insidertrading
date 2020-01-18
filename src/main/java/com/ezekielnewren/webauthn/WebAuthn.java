package com.ezekielnewren.webauthn;

import com.ezekielnewren.webauthn.data.RegistrationRequest;
import com.ezekielnewren.webauthn.data.RegistrationResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.NonNull;

import javax.servlet.http.HttpSession;
import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebAuthn implements Closeable {

    // https://developers.yubico.com/WebAuthn/Libraries/Using_a_library.html
    // https://developers.yubico.com/java-webauthn-server/

    final Object mutex = new Object();
    SecureRandom random;
    //MemoryCredentialRepository credRepo;
    RelyingPartyIdentity rpi;
    RelyingParty rp;
    ObjectMapper om;
    CredentialRepository credStore;

    Map<ByteArray, RegistrationRequest> requestMap = new HashMap<>();

    public WebAuthn(String fqdn, String title) {
        synchronized (mutex) {
            try {
                random = SecureRandom.getInstanceStrong();
            } catch (NoSuchAlgorithmException e) {
            }
            random = new SecureRandom();
            //credRepo = new MemoryCredentialRepository();
            //credStore = new InMemoryRegistrationStorage();
            credStore = new MemoryCredentialRepository();
            rpi = RelyingPartyIdentity.builder()
                    .id(fqdn)
                    .name(title)
                    .build();
            rp = RelyingParty.builder()
                    .identity(rpi)
                    .credentialRepository(credStore)
                    .build();
            om = new ObjectMapper()
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                    .registerModule(new Jdk8Module());
        }
    }

    public ObjectMapper getObjectMapper() {
        synchronized (mutex) {
            return om;
        }
    }

    public ByteArray generateRandom() {return generateRandom(32);}
    public ByteArray generateRandom(int size) {
        if (size <= 0) throw new IllegalArgumentException();
        byte[] tmp = new byte[size];
        random.nextBytes(tmp);
        return new ByteArray(tmp);
    }

    public String registerStart(
            @NonNull HttpSession session,
            @NonNull String username,
            Optional<String> displayName,
            Optional<String> credentialNickname,
            boolean requireResidentKey

    ) {
        synchronized (mutex) {
            try {
                //Optional<User> user = Optional.ofNullable((User) session.getAttribute(username));

                UserIdentity userIdentity = UserIdentity.builder()
                        .name(username)
                        .displayName(displayName.orElse(username))
                        .id(generateRandom())
                        .build();

                RegistrationRequest request = new RegistrationRequest(
                        username,
                        credentialNickname,
                        generateRandom(),
                        rp.startRegistration(
                                StartRegistrationOptions.builder()
                                        .user(userIdentity)
                                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                                .requireResidentKey(requireResidentKey)
                                                .build()
                                        )
                                        .build()
                        )
                );

                requestMap.put(request.getRequestId(), request);

                String json = null;
                try {
                    json = om.writeValueAsString(request);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                return json;
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }
    }

    public boolean registerFinish(HttpSession session, String data) {
        synchronized(mutex) {
            RegistrationResponse response;
            try {
                response = om.readValue(data, RegistrationResponse.class);
            } catch (IOException e) {
                return false;
            }

            ByteArray reqId = response.getRequestId();
            RegistrationRequest request = requestMap.get(reqId);
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkcid = response.getCredential();
            requestMap.remove(reqId);

            try {
                RegistrationResult registration = rp.finishRegistration(
                        FinishRegistrationOptions.builder()
                        .request(request.getPublicKeyCredentialCreationOptions())
                        .response(response.getCredential())
                        .build()
                );
            } catch (RegistrationFailedException e) {
                return false;
            }

            RegisteredCredential.builder()
                    .credentialId(null)
                    .userHandle(null)
                    .publicKeyCose(null)
                    .signatureCount(0)
                    .build();


            return true;
        }
    }

    @Override
    public void close() throws IOException {

    }


}
