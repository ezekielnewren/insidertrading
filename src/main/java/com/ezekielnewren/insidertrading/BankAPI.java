package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.Account;
import com.ezekielnewren.insidertrading.data.Transaction;
import com.ezekielnewren.insidertrading.data.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * This class is used by the front end for interfacing with the back.
 */
public class BankAPI {

    /**
     * {@code SessionManager} object.
     * @see com.ezekielnewren.insidertrading.SessionManager
     */
    SessionManager ctx;

    /**
     * Constructor for {@code BankAPI}.
     * @param _ctx context for {@code SessionManager}.
     * @see com.ezekielnewren.insidertrading.SessionManager
     */
    public BankAPI(SessionManager _ctx) {
        ctx = _ctx;
    }

    /**
     *
     * @param session
     * @param data
     * @return
     * @throws JsonProcessingException
     * @see javax.servlet.http.HttpSession
     * @see java.lang.String
     */
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
        else if(json.getString("cmd").equals("getTransactionHistory")){
            //Gets the list of accounts
            List<Account> result = getAccountList(session);

            //returns the history of each account
            return getTransactionHistory(result);
        }
        else {
            jsonOut = ctx.getObjectMapper().writeValueAsString(null);
        }


        return jsonOut;
    }

    /**
     * Get username from the {@code session}.
     * @param session current {@code session}.
     * @return the username from the {@code session}.
     * @see javax.servlet.http.HttpSession
     */
    public String getUsername(HttpSession session) {
        return ctx.getUsername(session);
    }

    /**
     * Checks if user is logged in, if they are gets account information for user.
     * @param session current {@code session}.
     * @return if not logged in null else account for the user.
     * @see javax.servlet.http.HttpSession
     */
    public List<Account> getAccountList(HttpSession session) {
        if (!ctx.isLoggedIn(session)) return null;
        String username = ctx.getUsername(session);

        User user = ctx.getUserStore().getByUsername(username);
        return user.getAccounts();
    }

    /**
     *
     * @param session
     * @param otherUser
     * @param accountTypeFrom
     * @param accountTypeTo
     * @param amount
     * @return
     * @see javax.servlet.http.HttpSession
     * @see java.lang.String
     */
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
        if(userTo == null) return false;

        Account acctFrom = userFrom.getAccount(accountTypeFrom.toString());
        Account acctTo = userTo.getAccount(accountTypeTo.toString());

        // transfer the money only if it makes sense e.g. the from account has at least the amount being transferred
        // your code here ...
        if(acctFrom.balance < amount){
            return false;
            //maybe return exception instead.
        }
        acctFrom.balance = acctFrom.balance - amount;
        acctTo.balance = acctTo.balance + amount;

        ctx.getUserStore().writeToDatabase(userFrom);
        ctx.getUserStore().writeToDatabase(userTo);
        Transaction t = new Transaction(acctFrom.getNumber(), acctTo.getNumber(), amount, System.currentTimeMillis());
        ctx.collectionTransaction.insertOne(t);
        return true;
    }

    /**
     *
     * @param aList
     * @return
     * @see java.util.List
     */
    public String getTransactionHistory(List<Account> aList){

        List<Transaction> tList = null;


        for(Account a : aList){
            tList.add((Transaction)ctx.collectionTransaction.find(Filters.eq("sendingAccount", a.getNumber())));
            tList.add((Transaction)ctx.collectionTransaction.find(Filters.eq("receivingAccount", a.getNumber())));
        }

        try {
            return ctx.getObjectMapper().writeValueAsString(tList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }




}
