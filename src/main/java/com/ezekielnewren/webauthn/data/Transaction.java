package com.ezekielnewren.webauthn.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;


@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@Getter
public class Transaction {

    @JsonProperty @NonNull final ObjectId _id;
    @JsonProperty long _sendingAccount;
    @JsonProperty long _receivingAccount;
    @JsonProperty @NonNull LocalDateTime _date;

    @JsonCreator
    public Transaction(@JsonProperty("_id")final ObjectId _id,
                       @JsonProperty("_sendingAccount") long _sendingAccount,
                       @JsonProperty("_receivingAccount") long _receivingAccount,
                       @JsonProperty("_date")LocalDateTime _date){
        this._id = _id;
        this._sendingAccount = _sendingAccount;
        this._receivingAccount = _receivingAccount;
        this._date = _date;
    }

    public Transaction(long _sendingAccount, long _receivingAccount, LocalDateTime _date){
        this(new ObjectId(), _sendingAccount, _receivingAccount, _date);
    }
}
