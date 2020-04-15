package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.webauthn.*;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.servlet.http.HttpSession;
import java.io.Closeable;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * The {@code WebAuthn} class handles the authentication of client and server.
 * */
public class WebAuthn /*implements Closeable*/ {

    // https://developers.yubico.com/WebAuthn/Libraries/Using_a_library.html
    // https://developers.yubico.com/java-webauthn-server/

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
     * Constant variable total length of an assertion.
     */
    public static final int LENGTH_ASSERTION_NONCE = 32;

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
        // yubikey metadata
        // https://developers.yubico.com/U2F/yubico-metadata.json
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

            MetadataService ms = (certificateList)-> {
                X509Certificate cert = certificateList.get(0);
                String name = cert.getClass().toGenericString();

                ArrayNode certificateListAsJsonPemArray = ctx.getObjectMapper().createArrayNode();
                for (X509Certificate item: certificateList) {
                    String value = Util.createPemFromX509Certificate(item);
                    certificateListAsJsonPemArray.add(value);
                }

                Map<String, String> deviceProperties = new LinkedHashMap<>();
                deviceProperties.put("attestationCertificateChain", certificateListAsJsonPemArray.toString());

                Attestation a = Attestation.builder()
                        .trusted(false)
                        .deviceProperties(deviceProperties)
                        .build();
                return a;
            };

            // ms = ctx.getMetadataService();

            rp = RelyingParty.builder()
                    .identity(rpi)
                    .credentialRepository(ctx.getUserStore().getCredentialRepository())
                    .allowOriginPort(true)
                    .allowOriginSubdomain(false)
//                    .allowUntrustedAttestation(false)
//                    .allowUnrequestedExtensions(true)
                    .metadataService(ms)
                    .attestationConveyancePreference(AttestationConveyancePreference.DIRECT)
                    .build();
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

                AuthenticatorSelectionCriteria asc = AuthenticatorSelectionCriteria.builder()
                        .requireResidentKey(requireResidentKey)
                        .userVerification(UserVerificationRequirement.DISCOURAGED)
                        .authenticatorAttachment(AuthenticatorAttachment.CROSS_PLATFORM)
                        .build();

                RegistrationRequest request = new RegistrationRequest(
                        username,
                        Optional.ofNullable(nickname),
                        Util.generateRandomByteArray(LENGTH_REQUEST_ID),
                        rp.startRegistration(
                                StartRegistrationOptions.builder()
                                        .user(userIdentity)
                                        .authenticatorSelection(asc)
                                        .timeout(Util.getRegistrationTimeout())
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
    public Triple<Boolean, RegistrationRequest, RegistrationResult> registerFinish(HttpSession session, RegistrationResponse response) throws IOException {
        synchronized(mutex) {
            ByteArray reqId = response.getRequestId();
            RegistrationRequest request = requestMap.remove(reqId);

            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> x = response.getCredential();
            AttestationObject ao = x.getResponse().getAttestation();
            AuthenticatorData ad = ao.getAuthenticatorData();
            AttestedCredentialData acd = ad.getAttestedCredentialData().orElseThrow();
            acd.getCredentialPublicKey();

            RegistrationResult result;
            try {
                result = rp.finishRegistration(
                        FinishRegistrationOptions.builder()
                                .request(request.getPublicKeyCredentialCreationOptions())
                                .response(response.getCredential())
                                .build()
                );
            } catch (RegistrationFailedException e) {
                return new ImmutableTriple<>(false, request, null);
            }

            boolean attestationTrusted = result.isAttestationTrusted();

            long now = System.currentTimeMillis();
            Authenticator auth = new Authenticator(
                    now,
                    now,
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

            return new ImmutableTriple<>(true, request, result);
        }
    }

    /**
     * <p>Creates a request using {@code requestId} and relying party {@code username}.
     * Maps the request using the {@code requestId} as key.</p>
     * @param username client user name.
     * @return requestId and relying party username.
     * @see java.lang.String
     */
    public <T> AssertionRequestWrapper<T> assertionStart(String username, ByteArray aad, T attachment) {
        synchronized(mutex) {
            if (aad == null) aad = new ByteArray(new byte[0]);

            ByteArray nonce = Util.generateRandomByteArray(LENGTH_ASSERTION_NONCE);
            ByteArray challenge = aad.concat(nonce);

            ByteArray requestId = generateRequestId();
            AssertionRequestWrapper request = new AssertionRequestWrapper(requestId,
                    Util.startAssertion(rp, StartAssertionOptions.builder()
                            .username(username)
                            .userVerification(UserVerificationRequirement.DISCOURAGED)
                            .timeout(Util.getAssertionTimeout())
                            .build(),
                            challenge
                            ),
                    attachment
            );

            assertMap.put(requestId, request);

            return request;
        }
    }

    /**
     * Checks to see if assertion is finished.
     * @param response response information from server.
     * @return returns {@code ImmutableTriple} with true on success.
     * @see com.ezekielnewren.insidertrading.data.AssertionResponse
     */
    public <T> Triple<Boolean, AssertionRequestWrapper<T>, AssertionResult> assertionFinish(AssertionResponse response) {
        synchronized(mutex) {
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
                    return new ImmutableTriple<>(false, request, result);
                }

                ctx.getUserStore().updateSignatureCount(result);
            } catch (AssertionFailedException e) {
                return new ImmutableTriple<>(false, request, null);
            }

            return new ImmutableTriple<>(true, request, result);
        }
    }

    /**
     * Checks if a user exists in the database upon logging in, if not creates one.
     * @param username user generated name.
     * @return null if name exists in database, else {@code request} information via {@code assertionStart} method.
     * @see java.lang.String
     */
    public AssertionRequestWrapper loginStart(String username) {
        synchronized(mutex) {
            if (!ctx.getUserStore().exists(username)) return null;
            return ctx.getWebAuthn().assertionStart(username, null, null);
        }
    }

    public String loginFinish(HttpSession httpSession, AssertionResponse response) {
        synchronized(mutex) {
            Triple<Boolean, AssertionRequestWrapper<Object>, AssertionResult> tuple = assertionFinish(response);
            if (tuple.getLeft()) {
                String username = tuple.getMiddle().getAssertionRequest().getUsername().get();
                ctx.setLoggedIn(httpSession, username);
                return username;
            }
            return null;
        }
    }

    /**
     * @param username
     * @param t
     * @return
     */
    public AssertionRequestWrapper signTransactionStart(String username, Transaction t) {
        synchronized(mutex) {
            return assertionStart(username, t.getBytesForSignature(ctx.getObjectMapper()), t);
        }
    }

    /**
     * @param httpSession
     * @param response
     * @return
     */
    public boolean signTransactionFinish(HttpSession httpSession, AssertionResponse response) {
        synchronized(mutex) {
            Triple<Boolean, AssertionRequestWrapper<Transaction>, AssertionResult> x = assertionFinish(response);
            if (x.getLeft()) {
                // unpack
                Transaction t = x.getMiddle().getAttachment();
                AssertionResult result = x.getRight();

                t.setSignature(response.getPublicKeyCredential());

                // continue with the transaction
                return true;
            }

            return false;
        }
    }

    /**
     * @param username
     * @param t
     * @return
     * @throws AssertionFailedException
     */
    public boolean verifyTransaction(String username, Transaction t) throws AssertionFailedException {
        synchronized(mutex) {
            ByteArray forsign = t.getBytesForSignature(ctx.getObjectMapper());


            PublicKeyCredential pkc = t.getSignature();
            ByteArray challenge = pkc.getResponse().getClientData().getChallenge();
            AssertionRequest request = Util.startAssertion(rp, StartAssertionOptions.builder().username(username).build(), challenge);

            if (!Arrays.equals(challenge.getBytes(), 0, forsign.size()-LENGTH_ASSERTION_NONCE, forsign.getBytes(), 0, forsign.size()-LENGTH_ASSERTION_NONCE)) return false;


            AssertionResult result;
            try {
                result = rp.finishAssertion(
                        FinishAssertionOptions.builder()
                                .request(request)
                                .response(pkc)
                                .build()
                    );
            } catch (AssertionFailedException e) {
                return false;
            }

            return result.isSuccess();
        }
    }

}
