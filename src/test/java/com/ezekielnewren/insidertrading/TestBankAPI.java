package com.ezekielnewren.insidertrading;

import com.ezekielnewren.Build;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpSession;

public class TestBankAPI {

    SessionManager ctx;
    BankAPI api;

    public TestBankAPI() {
        ctx = new SessionManager(
                JacksonHelper.newObjectMapper(),
                "mongodb://localhost",
                null,
                Build.get("fqdn"),
                Build.get("title")
        );
        api = ctx.getApi();
    }

    @Test
    public void getUsername() {
        HttpSession session = new DummyHttpSession();

        String username = api.getUsername(session);

        Assert.assertEquals(null, username);
    }



}
