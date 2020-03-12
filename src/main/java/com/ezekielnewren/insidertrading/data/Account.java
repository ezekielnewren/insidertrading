package com.ezekielnewren.insidertrading.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Account {
    public long number;
    public String title;
    public long balance;



//    @JsonCreator
//    public Account(
//            @JsonProperty("number") long _number,
//            @JsonProperty("title") String _title,
//            @JsonProperty("balance") long _balance
//    ) {
//        this.number = _number;
//        this.title = _title;
//        this.balance = _balance;
//    }
}
