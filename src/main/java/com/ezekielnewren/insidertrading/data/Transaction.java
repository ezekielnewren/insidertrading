package com.ezekielnewren.insidertrading.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.nio.charset.StandardCharsets;


/**
 * The class Transaction contains transaction information, constructs transaction {@code JSON} and constructs transactions.
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@Getter
public class Transaction {

    /**
     * 12-byte primary key value for transaction.
     */
    @JsonProperty @NonNull final ObjectId _id;

    /**
     * Sending account information.
     */
    @JsonProperty long sendingAccount;

    /**
     * Receiving account information.
     */
    @JsonProperty long receivingAccount;

    /**
     * Amount of the transaction.
     */
    @JsonProperty long amount;

    /**
     * Date of the transaction.
     */
    @JsonProperty long date;

    /**
     * Contains attributes returned to caller when credentials are created or an assertion is requested.
     * @see com.yubico.webauthn.data.PublicKeyCredential
     */
    @JsonProperty PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> signature;

    /**
     * Constructs {@code Transaction JSON} object using the transaction data.
     * @param _id unique transaction id.
     * @param _sendingAccount information about the sending account.
     * @param _receivingAccount information about the receiving account.
     * @param _amount amount of the transaction.
     * @param _date date of the transaction.
     * @param _signature contains attributes generated on credential creation or assertion request.
     */
    @JsonCreator
    public Transaction(@JsonProperty("_id") final ObjectId _id,
                       @JsonProperty("sendingAccount") long _sendingAccount,
                       @JsonProperty("receivingAccount") long _receivingAccount,
                       @JsonProperty("amount") long _amount,
                       @JsonProperty("date") long _date,
                       @JsonProperty("signature") PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> _signature
    ) {
        this._id = _id;
        this.sendingAccount = _sendingAccount;
        this.receivingAccount = _receivingAccount;
        this.amount = _amount;
        this.date = _date;
        this.signature = _signature;
    }

    /**
     * {@code Transaction} constructor for a new transaction.
     * @param _sendingAccount information about the sending account.
     * @param _receivingAccount information about the receiving account.
     * @param _amount amount of the transaction.
     * @param _date date of the transaction.
     * @param _signature contains attributes generated on credential creation or assertion request.
     */
    public Transaction(long _sendingAccount, long _receivingAccount, long _amount, long _date, PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> _signature) {
        this(new ObjectId(), _sendingAccount, _receivingAccount, _amount, _date, _signature);
    }

    /**
     * Converts an {@code ObjectMapper} containing transaction data to an equivalent {@code ByteArray}
     * @param om {@code ObjectMapper} containing transaction data.
     * @return new {@code ByteArray} of transaction data.
     * @see com.fasterxml.jackson.databind
     */
    public ByteArray getBytesForSignature(ObjectMapper om) {
        ObjectNode json = om.createObjectNode();

        json.put("sendingAccount", sendingAccount);
        json.put("receivingAccount", receivingAccount);
        json.put("amount", amount);
        json.put("date", date);

        return new ByteArray(json.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Setter for the {@code signature} attributes.
     * @param signature contains attributes for credential creation and assertion requests.
     * @see com.yubico.webauthn.data.PublicKeyCredential
     */
    public void setSignature(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> signature) {
        this.signature = signature;
    }

    /**
     * Serializes {@code ObjectMapper} to {@code String}
     * @param om {@code JSON} data.
     * @return {@code ObjectMapper} values as {@code String}
     */
    public String toString(ObjectMapper om) {
        try {
            return om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}














































