package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.*;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.NonNull;

import javax.servlet.http.HttpSession;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The {@code WebAuthn} class handles the authentication of client and server.
 * */
public class WebAuthn implements Closeable {

    // https://developers.yubico.com/WebAuthn/Libraries/Using_a_library.html
    // https://developers.yubico.com/java-webauthn-server/


    /**
     * Constant variable total length of user name.
     */
    public static final int LENGTH_USER_HANDLE = 12;

    /**
     * Constant variable total length of the request id.
     */
    public static final int LENGTH_REQUEST_ID = 16;

    /**
     * Constant variable total length of credential id.
     */
    public static final int LENGTH_CREDENTIAL_ID = 16;

    /**
     * Constant variable for servlet context.
     */
    final SessionManager ctx;

    /**
     * Constant variable mutex with nullcheck parameter.
     */
    final @NonNull Object mutex;

    /**
     * Variable for {@code RelayingPartyIdentity}.
     */
    RelyingPartyIdentity rpi;

    /**
     * Variable {@code RelayingParty} information.
     */
    RelyingParty rp;
    //ObjectMapper om;
    //CredentialRepository credStore;

    /**
     * Object that holds a {@code Map} for registration request information.
     */
    Map<ByteArray, RegistrationRequest> requestMap = new HashMap<>();

    /**
     * Object that holds a {@code Map} for assertion information.
     */
    Map<ByteArray, AssertionRequestWrapper> assertMap = new HashMap<>();


    /**
     * Constructor {@code Builders RelyingPartyIdentity} and {@code RelyingParty}.
     * @param _ctx the context of the servlet.
     * @param fqdn fully qualified domain name.
     * @param title client user name.
     */
    public WebAuthn(final SessionManager _ctx, String fqdn, String title) {
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

    /**
     * Generates a random user name.
     * @return ByteArray of that user handle.
     */
    public static ByteArray generateUserHandle() {
        return Util.generateRandomByteArray(LENGTH_USER_HANDLE);
    }

    /**
     * Generates random request id.
     * @return ByteArray of that request id.
     */
    public static ByteArray generateRequestId() {
        return Util.generateRandomByteArray(LENGTH_REQUEST_ID);
    }

    /**
     * Generates a random credential id.
     * @return ByteArray of that credential id.
     */
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

    /**
     * <p>Contains the information necessary to build a {@code RegistrationRequest}: on success, userIdentity and request.
     * Maps request to the request map.</p>
     * @param session currently not in use.
     * @param username clients name.
     * @param displayName clients display name.
     * @param nickname optional client name field.
     * @param requireResidentKey specify requirements regarding authenticator.
     * @return RegistrationRequest, request, containing necessary information.
     */
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

                // that username has been taken
                if (ctx.getUserStore().exists(username)) {
                    return null;
                }

                // you must log out before creating a new account
                if (ctx.isLoggedIn(session)) {
                    return null;
                }

                UserIdentity userIdentity = UserIdentity.builder()
                        .name(username)
                        .displayName(displayName)
                        .id(Util.generateRandomByteArray(LENGTH_USER_HANDLE))
                        .build();

                RegistrationRequest request = new RegistrationRequest(
                        username,
                        Optional.ofNullable(nickname),
                        Util.generateRandomByteArray(LENGTH_REQUEST_ID),
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

    /**
     * <p>Update results with completed registration object.
     * Updates username, displayname, and auth.
     * Adds authenticator with client information to ctx.</p>
     * @param session currently not in use.
     * @param response response information from server.
     * @return returns true on success.
     * @throws IOException throws, never caught.
     * @see javax.servlet.http.HttpSession
     * @see com.ezekielnewren.insidertrading.data.RegistrationResponse
     */
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

            ctx.getUserStore().addAuthenticator(username, displayName, auth, null, null, 0, 0, 0);
            ctx.setLoggedIn(session, username);

            return true;
        }
    }

    /**
     * <p>Creates a request using {@code requestId} and relying party {@code username}.
     * Maps the request using the {@code requestId} as key.</p>
     * @param username client user name.
     * @return requestId and relying party username.
     * @see java.lang.String
     */
    public AssertionRequestWrapper assertionStart(String username) {

        ByteArray requestId = generateRequestId();
        AssertionRequestWrapper request = new AssertionRequestWrapper(requestId,
                rp.startAssertion(StartAssertionOptions.builder()
                        .username(username)
                        .build())
        );

        rp.startAssertion(StartAssertionOptions.builder()
                .username("")
                .build()
        );

        assertMap.put(requestId, request);

        return request;
    }

    /**
     * Checks to see if assertion is finished.
     * @param response response information from server.
     * @return returns true if finished.
     * @see com.ezekielnewren.insidertrading.data.AssertionResponse
     */
    public boolean assertionFinish(AssertionResponse response) {

        AssertionRequestWrapper request = assertMap.remove(response.getRequestId());

        AssertionResult result = null;
        try {
             result = rp.finishAssertion(
                    FinishAssertionOptions.builder()
                    .request(request.getAssertionRequest())
                    .response(response.getPublicKeyCredential())
                    .build()
            );

            if (!(result.isSuccess() && result.isSignatureCounterValid())) {
                return false;
            }

            ctx.getUserStore().updateSignatureCount(result);
        } catch (AssertionFailedException e) {
            return false;
        }

        return true;
    }

    public AssertionRequestWrapper loginStart(String username) {
        return ctx.getWebAuthn().assertionStart(username);
    }

    public boolean loginFinish(HttpSession httpSession, AssertionResponse response) {
        AssertionRequestWrapper request = assertMap.get(response.getRequestId());
        if (ctx.getWebAuthn().assertionFinish(response)) {
            ctx.setLoggedIn(httpSession, request.getAssertionRequest().getUsername().get());
            return true;
        }
        return false;
    }

    /**
     * <p>Closes this stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.</p>
     * @throws IOException throws, never caught.
     */
    @Override
    public void close() throws IOException {

    }


}
