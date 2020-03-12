package com.ezekielnewren.insidertrading.data;


public class Account {
    public long number;
    public String title;
    public long balance;

    public Account(long _number, String _title, long _balance){
        this.number = _number;
        this.title = _title;
        this.balance = _balance;
    }
}
