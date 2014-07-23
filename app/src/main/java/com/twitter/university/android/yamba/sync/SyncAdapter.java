package com.twitter.university.android.yamba.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.twitter.university.android.yamba.YambaApplication;
import com.twitter.university.android.yamba.svc.YambaLogic;

import java.io.IOException;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC";


    private final Context ctxt;

    public SyncAdapter(Context ctxt, boolean autoInitialize) {
        super(ctxt, autoInitialize);
        this.ctxt = ctxt;
    }

    @Override
    public void onPerformSync(
        Account account,
        Bundle extras,
        String authority,
        ContentProviderClient provider,
        SyncResult syncResult)
    {
        String token = null;
        YambaClient client = null;
        while (true) {
            Exception e = null;
            try {
                token = AccountManager.get(ctxt).blockingGetAuthToken(
                    account,
                    AccountMgr.AUTH_TYPE_CLIENT,
                    false);
            }
            catch (OperationCanceledException oce) { e = oce; }
            catch (AuthenticatorException ae) { e = ae; }
            catch (IOException ioe) { e = ioe; }
            if (null != e) { Log.e(TAG, "failed getting token", e); }

            if (null == token) { return; }

            client = ((YambaApplication) ctxt.getApplicationContext()).getClientByToken(token);
            if (null != client) { break; }

            AccountManager.get(ctxt).invalidateAuthToken(account.type, token);
        }

        new YambaLogic(ctxt).doSync(client);
    }
}
