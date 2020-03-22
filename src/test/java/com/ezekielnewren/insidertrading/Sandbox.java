package com.ezekielnewren.insidertrading;

import com.ezekielnewren.Build;
import com.ezekielnewren.insidertrading.data.*;
import com.ezekielnewren.insidertrading.JacksonHelper;
import com.ezekielnewren.insidertrading.InsiderTradingServlet;
import com.ezekielnewren.insidertrading.data.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Sandbox {

//    @JsonProperty
//    @NonNull final ObjectId _id;
//    @JsonProperty @NonNull String username;
//    @JsonProperty @NonNull
//    Optional<String> displayName;
//    @JsonProperty @NonNull
//    List<String> email;
//    @JsonProperty @NonNull List<Authenticator> authenticator;
//    @JsonProperty @NonNull String firstName;
//    @JsonProperty @NonNull String lastName;
//    @JsonProperty int ssn;
//    @JsonProperty long savingAccount;
//    @JsonProperty long checkingAccount;

    public static void main(String[] args) throws Exception {
        ObjectMapper om = JacksonHelper.newObjectMapper();
        /*
        ArrayList<String> email = new ArrayList<String>();
        email.add("bradpeterson@weber.edu");
        ArrayList<Account> accounts = new ArrayList<>();
        Account savings = new Account(5, "Savings", 578L);
        Account checking = new Account(477, "Savings", 0L);
        accounts.add(savings);
        accounts.add(checking);
        LocalDateTime time = LocalDateTime.now();

        User u = new User("drpeterson", "Dr. Brad Peterson", email, new ArrayList<>(), "Brad", "Peterson", 101001011, accounts);
        Transaction t = new Transaction(savings, checking, 88888888L, time);
        String json = om.writeValueAsString(t);

        InsiderTradingServletContext ctx = new InsiderTradingServletContext(
                om,
                "mongodb://localhost",
                null,
                Build.get("fqdn"),
                Build.get("title")
        );

        System.out.println(json);

        //MongoCollection<User> userColl = ctx.collectionUser;
        MongoCollection<Transaction> transactionColl = ctx.collectionTransaction;
        //userColl.insertOne(u);
        transactionColl.insertOne(t);
         */

        // dataColl.insertOne(Document.parse(json));


    }

}
