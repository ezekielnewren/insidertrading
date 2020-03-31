package com.ezekielnewren.insidertrading.data;


import com.ezekielnewren.insidertrading.Util;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import org.bson.types.ObjectId;

/**
 * Class that contains data about an {@code Account}
 */
public class Account {

    /**
     * Instance variable that holds the {@code MongoDB} id associated with an account.
     * @see org.bson.types.ObjectId
     */
    @JsonProperty final ObjectId _id;

    /**
     * Instance variable that holds the number associated with an account.
     */
    @JsonProperty long number;

    /**
     * Instance variable that holds the title for an account.
     *
     * <p>
     *     There can be many title (types) of accounts withing an account.
     * </p>
     */
    @NonNull public String title;

    /**
     * Instance variable that holds the amount of money in an account.
     */
    public long balance;

    /**
     * Object that holds Default account types for an account.
     */
    public enum DefaultNames {
        Savings,
        Checking,
        MoneyMarket
    }

    /**
     * {@code JSON} object for an account.
     * @param _id unique id.
     * @param _number account number, separate from account id.
     * @param _title type of account.
     * @param _balance number of cents.
     * @see org.bson.types.ObjectId
     * @see java.lang.String
     */
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
     * Create a new account with an {@code ObjectId} created by a csprng rather
     * than the mongo default for {@code ObjectId}
     * @param _title what kind of financial account e.g. Savings, Checking.
     * @param _balance the number of cents.
     */
    public Account(String _title, long _balance) {
        this(Util.generateRandomObjectId(), Util.getRandom().nextInt(Integer.MAX_VALUE), _title, _balance);
    }

    /**
     * Gets the account number.
     * @return the account number.
     */
    public long getNumber(){
        return this.number;
    }
}
