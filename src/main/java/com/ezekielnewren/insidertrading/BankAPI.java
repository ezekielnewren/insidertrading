package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.Account;
import com.ezekielnewren.insidertrading.data.Transaction;
import com.ezekielnewren.insidertrading.data.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

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

    static final Map<String, Pair<Method, JsonProperty[]>> call = new LinkedHashMap<>();
    static {
        List<Method> methList = Arrays.asList(BankAPI.class.getMethods());
        for (Method m: methList) {

            if (m.getParameterCount() <= 0) continue;
            if (m.getParameterTypes()[0] != HttpSession.class) continue;

            Annotation[][] annmat = m.getParameterAnnotations();
            JsonProperty[] prop = new JsonProperty[annmat.length-1];
            boolean good = true;
            for (int i=0; i<prop.length; i++) {
                if (annmat[i+1].length != 1 || !(annmat[i+1][0] instanceof JsonProperty)) {
                    good = false;
                    break;
                }
                prop[i] = (JsonProperty) annmat[i+1][0];
            }
            if (!good) continue;

            call.put(m.getName(), new ImmutablePair<>(m, prop));
        }
    }

    /**
     * Constructor for {@code BankAPI}.
     * @param _ctx context for {@code SessionManager}.
     * @see com.ezekielnewren.insidertrading.SessionManager
     */
    public BankAPI(SessionManager _ctx) {
        ctx = _ctx;
    }

    public static String generateJSFunction() {
        StringBuilder sb = new StringBuilder();

        for (String command: call.keySet()) {
            JsonProperty[] prop = call.get(command).getRight();
            String[] args = new String[prop.length];

            for (int i=0; i<args.length; i++) {
                args[i] = prop[i].value();
            }

            String view = StringUtils.join(args, ", ");

            sb.append("function "+command+"("+view+") {\n");
            sb.append("    \"use strict\";\n");
            sb.append("    return makeRequest(\""+command+"\", {"+view+"});\n");
            sb.append("}\n");
            sb.append("\n");
        }

        return sb.toString();
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
    ObjectNode onRequest(HttpSession session, String data) {
        ObjectNode response = ctx.getObjectMapper().createObjectNode();
        JsonNode request;
        try {
            request = ctx.getObjectMapper().readTree(data);
        } catch (JsonProcessingException e) {
            response.put("error", "cannot parse request");
            response.put("data", (String)null);
            return response;
        }

        Pair<Method, JsonProperty[]> tuple = call.get(request.get("cmd").asText());
        if (tuple == null) {
            response.put("error", BankAPIException.Reason.NO_SUCH_FUNCTION.toString());
            response.putPOJO("data", null);
            return response;
        }
        Method m = tuple.getLeft();
        JsonProperty[] prop = tuple.getRight();

        Object[] args = new Object[m.getParameterCount()];
        args[0] = session;
        for (int i=0; i<prop.length; i++) {
            args[i+1] = Util.asPOJO(request.get("args").get(prop[i].value()));
        }

        try {
            Object result = m.invoke(this, args);
            response.put("error", (String) null);
            response.putPOJO("data", result);
        } catch (BankAPIException e) {
            response.put("error", e.getMessage());
            response.putPOJO("data", null);
        } catch (IllegalAccessException|InvocationTargetException e) {
            response.put("error", BankAPIException.Reason.ILLEGAL_ACCESS.toString());
            response.putPOJO("data", null);
        }

        return response;
    }

    /**
     * Checks if user is logged in, if they are gets account information for user.
     * @param session current {@code session}.
     * @return if not logged in null else account for the user.
     * @see javax.servlet.http.HttpSession
     */
    public List<Account> getAccountList(HttpSession session) throws BankAPIException {
        if (!ctx.isLoggedIn(session)) throw new BankAPIException(BankAPIException.Reason.NOT_LOGGED_IN);
        String username = ctx.getUsername(session);

        User user = ctx.getUserStore().getByUsername(username);
        return user.getAccounts();
    }

    /**
     * Get username from the {@code session}.
     * @param session current {@code session}.
     * @return the username from the {@code session}.
     * @see javax.servlet.http.HttpSession
     */
    public String getUsername(HttpSession session) throws BankAPIException {
        return ctx.getUsername(session);
    }

    /**
     * @return
     * @see java.util.List
     */
    public List<Transaction> getTransactionHistory(HttpSession session) throws BankAPIException {

        String userN = getUsername(session);
        User u = ctx.getUserStore().getByUsername(userN);
        List<Account> aList = u.getAccounts();

        List<Transaction> tList = new ArrayList<>();

        for(Account a : aList){
            tList.add((Transaction)ctx.collectionTransaction.find(Filters.eq("sendingAccount", a.getNumber())));
            tList.add((Transaction)ctx.collectionTransaction.find(Filters.eq("receivingAccount", a.getNumber())));
        }

        return tList;
    }

    public String logout(HttpSession session) throws BankAPIException {
        String username = ctx.getUsername(session);
        ctx.clearLoggedIn(session);
        return username;
    }

    /**
     *
     * @param session
     * @param recipient
     * @param accountTypeFrom
     * @param accountTypeTo
     * @param amount
     * @return true if it is allowed to go through
     * @see javax.servlet.http.HttpSession
     * @see java.lang.String
     */
    public boolean transfer(HttpSession session,
                            @JsonProperty("recipient") String recipient,
                            @JsonProperty("accountTypeFrom") String accountTypeFrom,
                            @JsonProperty("accountTypeTo") String accountTypeTo,
                            @JsonProperty("amount") long amount
    ) throws BankAPIException {
        // argument checking
        Objects.nonNull(recipient);
        Objects.nonNull(accountTypeFrom);
        Objects.nonNull(accountTypeTo);
        if (amount < 0) throw new IllegalArgumentException();

        String from = getUsername(session);
        if (from == null) return false; // not logged in
        String to = recipient; // do they exist? if not return false

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
}
