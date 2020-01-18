package com.ezekielnewren.webauthn.data;

import java.util.HashMap;
import java.util.Map;

public class UserStore {

    protected final Object mutex;
    Map<String, User> store = new HashMap<>();

    public UserStore(final Object _mutex) {
        this.mutex = _mutex;
    }

//    public boolean isLoggedIn(String username) {
//        synchronized(username) {
//            store.containsKey(username);
//
//        }
//    }






}
