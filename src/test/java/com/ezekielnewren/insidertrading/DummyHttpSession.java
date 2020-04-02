package com.ezekielnewren.insidertrading;

import com.yubico.webauthn.data.ByteArray;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.ws.rs.core.Link;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DummyHttpSession implements HttpSession {

    ByteArray id;
    long creationTime;
    long lastAccessedTime;
    int maxInactiveInterval;
    Map<String, Object> attribute = new LinkedHashMap<>();
    Map<String, Object> value = new LinkedHashMap<>();

    public DummyHttpSession(ByteArray _id, long _creationTime, long _lastAccessedTime) {
        id = _id;
        creationTime = _creationTime;
        lastAccessedTime = _lastAccessedTime;
    }

    public DummyHttpSession() {
        this(Util.generateRandomByteArray(16), System.currentTimeMillis(), System.currentTimeMillis());
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id.getHex();
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return attribute.get(name);
    }

    @Override
    public Object getValue(String name) {
        return value.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new Enumeration<String>() {
            Iterator<String> it = attribute.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public String nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public String[] getValueNames() {
        String[] tmp = new String[value.size()];
        int i=0;
        for (String key: value.keySet()) tmp[i++] = key;
        return tmp;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attribute.put(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        attribute.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attribute.remove(name);
    }

    @Override
    public void removeValue(String name) {
        value.remove(name);
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isNew() {
        return false;
    }
}
