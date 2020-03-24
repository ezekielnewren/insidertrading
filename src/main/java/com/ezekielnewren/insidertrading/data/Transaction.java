package com.ezekielnewren.insidertrading.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Optional;


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

    @JsonProperty PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> signature;

    /**
     * Constructs {@code Transaction JSON} object using the transaction data.
     * @param _id unique transaction id.
     * @param _sendingAccount information about the sending account.
     * @param _receivingAccount information about the receiving account.
     * @param _amount amount of the transaction.
     * @param _date date of the transaction.
     */
    @JsonCreator
    public Transaction(@JsonProperty("_id") final ObjectId _id,
                       @JsonProperty("sendingAccount") long _sendingAccount,
                       @JsonProperty("receivingAccount") long _receivingAccount,
                       @JsonProperty("amount") long _amount,
                       @JsonProperty("date") long _date){
        this._id = _id;
        this.sendingAccount = _sendingAccount;
        this.receivingAccount = _receivingAccount;
        this.amount = _amount;
        this.date = _date;
    }

    /**
     * {@code Transaction} constructor for a new transaction.
     * @param _sendingAccount information about the sending account.
     * @param _receivingAccount information about the receiving account.
     * @param _amount amount of the transaction.
     * @param _date date of the transaction
     */
    public Transaction(long _sendingAccount, long _receivingAccount, long _amount, long _date){
        this(new ObjectId(), _sendingAccount, _receivingAccount, _amount, _date);
    }

    public ByteArray getBytesForSignature(ObjectMapper om) {
        ObjectNode json = om.createObjectNode();

        json.put("sendingAccount", sendingAccount);
        json.put("receivingAccount", receivingAccount);
        json.put("amount", amount);
        json.put("date", date);

        return new ByteArray(json.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void setSignature(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> signature) {
        this.signature = signature;
    }
}














































