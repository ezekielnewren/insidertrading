package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.ByteArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 *
 */
@AllArgsConstructor
@Getter
public class AssertionRequestWrapper {

    /**
     *
     */
    @JsonProperty @NonNull ByteArray requestId;


    /**
     *
     */
    @JsonProperty @NonNull AssertionRequest assertionRequest;

}
