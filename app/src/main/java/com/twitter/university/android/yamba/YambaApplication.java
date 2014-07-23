package com.twitter.university.android.yamba;

import android.accounts.Account;
import android.app.Application;

import com.marakana.android.yamba.clientlib.YambaClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class YambaApplication extends Application {
    private static final String TAG = "APP";

    private String token;
    private YambaClient client;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public synchronized YambaClient getClientByToken(String tkn) {
        return ((null == this.token) || !this.token.equals(tkn))
            ? null
            : client;
    }

    public synchronized void createClient(String tkn, String handle, String pwd, String uri) {
        token = tkn;
        client = new YambaClient(handle, pwd, uri);
    }
}
