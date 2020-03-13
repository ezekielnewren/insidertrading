package com.ezekielnewren.insidertrading.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonProperty long sendingAccount;
    @JsonProperty long receivingAccount;
    @JsonProperty long amount;
    @JsonIgnore @NonNull long date;

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

    public Transaction(long _sendingAccount, long _receivingAccount, long _amount, long _date){
        this(new ObjectId(), _sendingAccount, _receivingAccount, _amount, _date);
    }
}
