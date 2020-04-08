package com.ezekielnewren.insidertrading.data;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Transfer {
    public String fromUser;
    public String fromAccountType;
    public String toUser;
    public String toAccountType;
    public long amount;
    public long date;
}
