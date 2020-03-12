package com.ezekielnewren.insidertrading.data;


import com.ezekielnewren.insidertrading.Util;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import org.bson.types.ObjectId;

public class Account {
    @JsonProperty final ObjectId _id;
    @JsonProperty long number;
    @NonNull public String title;
    public long balance;

    public enum DefaultNames {
        Savings,
        Checking,
        MoneyMarket
    }

    @JsonCreator
    Account(
            @JsonProperty("_id") ObjectId _id,
            @JsonProperty("number") long _number,
            @JsonProperty("title") String _title,
            @JsonProperty("balance") long _balance
    ) {
        this._id = _id;
        this.number = _number;
        this.title = _title;
        this.balance = _balance;
    }

    /**
     * Create a new account with an ObjectId created by a csprng rather
     * than the mongo default for ObjectId
     * @param _title what kind of financial accout e.g. Savings, Checking
     * @param _balance the number of cents
     */
    public Account(String _title, long _balance) {
        this(Util.generateRandomObjectId(), Util.getRandom().nextInt(Integer.MAX_VALUE), _title, _balance);
    }
}
