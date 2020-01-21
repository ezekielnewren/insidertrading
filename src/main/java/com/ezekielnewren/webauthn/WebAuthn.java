package com.ezekielnewren.webauthn;

import com.ezekielnewren.webauthn.data.Authenticator;
import com.ezekielnewren.webauthn.data.RegistrationRequest;
import com.ezekielnewren.webauthn.data.RegistrationResponse;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.NonNull;

import javax.servlet.http.HttpSession;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebAuthn implements Closeable {

    // https://developers.yubico.com/WebAuthn/Libraries/Using_a_library.html
    // https://developers.yubico.com/java-webauthn-server/

    public static final int LENGTH_USER_HANDLE = 12;
    public static final int LENGTH_REGISTRATION_REQUEST = 16;
    public static final int LENGTH_CREDENTIAL_ID = 16;

    final AuthServletContext ctx;
    final @NonNull Object mutex;

    RelyingPartyIdentity rpi;
    RelyingParty rp;
    //ObjectMapper om;
    //CredentialRepository credStore;

    Map<ByteArray, RegistrationRequest> requestMap = new HashMap<>();

    public WebAuthn(final AuthServletContext _ctx, String fqdn, String title) {
        this.ctx = _ctx;
        this.mutex = ctx.getMutex();
        synchronized (mutex) {
            //credRepo = new MemoryCredentialRepository();
            //credStore = new InMemoryRegistrationStorage();
            //credStore = new MemoryCredentialRepository();
            rpi = RelyingPartyIdentity.builder()
                    .id(fqdn)
                    .name(title)
                    .build();
            rp = RelyingParty.builder()
                    .identity(rpi)
                    .credentialRepository(ctx.getUserStore().getCredentialRepository())
                    .allowOriginPort(true)
                    .allowOriginSubdomain(false)
                    .build();
//            om = new ObjectMapper()
//                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
//                    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
//                    .registerModule(new Jdk8Module());
        }
    }

    public static ByteArray generateUserHandle() {
        return Util.generateRandomByteArray(LENGTH_USER_HANDLE);
    }

    public static ByteArray generateRegistrationRequestId() {
        return Util.generateRandomByteArray(LENGTH_REGISTRATION_REQUEST);
    }

    public static ByteArray generateCredentialId() {
        return Util.generateRandomByteArray(LENGTH_CREDENTIAL_ID);
    }


//    public ObjectMapper getObjectMapper() {
//        synchronized (mutex) {
//            return om;
//        }
//    }

//    public static ByteArray generateRandom() {return generateRandom(32);}
//    public static ByteArray generateRandom(int size) {
//        if (size <= 0) throw new IllegalArgumentException();
//        byte[] tmp = new byte[size];
//        new SecureRandom().nextBytes(tmp);
//        return new ByteArray(tmp);
//    }

    public RegistrationRequest registerStart(
            @NonNull HttpSession session,
            @NonNull String username,
            String displayName,
            String nickname,
            boolean requireResidentKey

    ) {
        synchronized (mutex) {
            try {
                //Optional<User> user = Optional.ofNullable((User) session.getAttribute(username));

                UserIdentity userIdentity = UserIdentity.builder()
                        .name(username)
                        .displayName(displayName)
                        .id(Util.generateRandomByteArray(LENGTH_USER_HANDLE))
                        .build();

                RegistrationRequest request = new RegistrationRequest(
                        username,
                        Optional.ofNullable(nickname),
                        Util.generateRandomByteArray(LENGTH_REGISTRATION_REQUEST),
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

                return request;
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }
    }

    public boolean registerFinish(HttpSession session, RegistrationResponse response) throws IOException {
        synchronized(mutex) {
            ByteArray reqId = response.getRequestId();
            RegistrationRequest request = requestMap.remove(reqId);

            RegistrationResult result;
            try {
                result = rp.finishRegistration(
                        FinishRegistrationOptions.builder()
                        .request(request.getPublicKeyCredentialCreationOptions())
                        .response(response.getCredential())
                        .build()
                );
            } catch (RegistrationFailedException e) {
                return false;
            }

            Authenticator auth = new Authenticator(
                    System.currentTimeMillis(),
                    result.getKeyId().getId(),
                    result.getPublicKeyCose(),
                    0,
                    request.getNickname(),
                    result.getAttestationMetadata(),
                    result.getAttestationType()
            );

            String username = request.getUsername();
            String displayName = request.getPublicKeyCredentialCreationOptions().getUser().getDisplayName();

            ctx.getUserStore().addAuthenticator(username, displayName, auth);

            return true;
        }
    }

    @Override
    public void close() throws IOException {

    }


}
