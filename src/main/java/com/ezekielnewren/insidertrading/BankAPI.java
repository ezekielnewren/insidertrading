package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.Account;
import com.ezekielnewren.insidertrading.data.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used by the front end for interfacing with the back.
 */
public class BankAPI {

    SessionManager ctx;

    public BankAPI(SessionManager _ctx) {
        ctx = _ctx;
    }

    String onRequest(HttpSession session, String data) throws JsonProcessingException {
        JSONObject json = new JSONObject(data);
        String jsonOut;

        String whoami = getUsername(session);

        if (json.getString("cmd").equals("getUsername")) {
            // no arguments for this command

            // call the function
            String result = getUsername(session);

            // turn result into json
            jsonOut = ctx.getObjectMapper().writeValueAsString(result);
        } else if (json.getString("cmd").equals("getAccountList")) {
            // no arguments for this command

            // call the function
            List<Account> result = getAccountList(session);

            // turn result into json
            jsonOut = ctx.getObjectMapper().writeValueAsString(result);
        } else if (json.getString("cmd").equals("transfer")) {
            // extract arguments from json
            JSONObject args = json.getJSONObject("args");
            String otherUser = args.getString("otherUser");
            String accountTypeFrom = args.getString("accountTypeFrom");
            String accountTypeTo = args.getString("accountTypeTo");
            long amount = args.getLong("amount");

            // call the function
            boolean result = transfer(session, otherUser, accountTypeFrom, accountTypeTo, amount);

            // turn result into json
            jsonOut = ctx.getObjectMapper().writeValueAsString(result);
        }
        else {
            jsonOut = ctx.getObjectMapper().writeValueAsString(null);
        }


        return jsonOut;
    }

    public String getUsername(HttpSession session) {
        return ctx.getUsername(session);
    }

    public List<Account> getAccountList(HttpSession session) {
        if (!ctx.isLoggedIn(session)) return null;
        String username = ctx.getUsername(session);

        User user = ctx.getUserStore().getByUsername(username);
        return user.getAccounts();
    }

    public boolean transfer(HttpSession session, String otherUser,
                            String accountTypeFrom, String accountTypeTo, long amount) {
        // argument checking
        Objects.nonNull(otherUser);
        Objects.nonNull(accountTypeFrom);
        Objects.nonNull(accountTypeTo);
        if (amount < 0) throw new IllegalArgumentException();

        String from = getUsername(session);
        if (from == null) return false; // not logged in
        String to = otherUser; // do they exist? if not return false

        User userFrom = ctx.getUserStore().getByUsername(from);
        User userTo = ctx.getUserStore().getByUsername(to);

        Account acctFrom = userFrom.getAccount(accountTypeFrom.toString());
        Account acctTo = userTo.getAccount(accountTypeTo.toString());

        // transfer the money only if it makes sense e.g. the from account has at least the amount being transferred
        // your code here ...

        ctx.getUserStore().writeToDatabase(userFrom);
        ctx.getUserStore().writeToDatabase(userTo);

        return true;
    }




}
