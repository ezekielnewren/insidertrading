package com.ezekielnewren.insidertrading;

import com.ezekielnewren.Build;
import lombok.SneakyThrows;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CryptoManager {
    private static final CryptoManager instance = new CryptoManager();

    KeyStore keyStore;
    X509TrustManager tm;

    @SneakyThrows
    private CryptoManager() {
        keyStore = KeyStore.getInstance("JKS");
        keyStore.load(Build.getResource("truststore.jks"), null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        TrustManager[] tma = tmf.getTrustManagers();
        tm = (X509TrustManager) tma[0];
    }

    public static CryptoManager getInstance() {
        return instance;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public X509TrustManager getX509TrustManager() {
        return tm;
    }

    @SneakyThrows
    public X509Certificate getX509Certificate(String alias) {
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    @SneakyThrows
    X509TrustManager createX509TrustManager0(List<X509Certificate> list, KeyStore _ks) {
        _ks = KeyStore.getInstance("JKS");
        _ks.load(null, null);

        for (X509Certificate item: list) {
            _ks.setCertificateEntry(item.getSubjectDN().getName(), item);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(_ks);

        return (X509TrustManager) tmf.getTrustManagers()[0];
    }

    @SneakyThrows
    public X509TrustManager createX509TrustManager(List<X509Certificate> list) {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);

        return createX509TrustManager0(list, ks);
    }

    public X509TrustManager createX509TrustManager(X509Certificate cert) {
        ArrayList<X509Certificate> list = new ArrayList<>();
        list.add(cert);
        return createX509TrustManager(list);
    }

    public X509TrustManager createX509TrustManager(X509Certificate[] list) {
        return createX509TrustManager(Arrays.asList(list));
    }
}




