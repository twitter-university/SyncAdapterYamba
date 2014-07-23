/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.twitter.university.android.yamba.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.twitter.university.android.yamba.BuildConfig;
import com.twitter.university.android.yamba.NewAccountActivity;
import com.twitter.university.android.yamba.R;
import com.twitter.university.android.yamba.YambaApplication;

import java.util.UUID;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
public class AccountMgr extends AbstractAccountAuthenticator {
    private static final String TAG = "ACCT";

    public static final String KEY_HANDLE = "YambaAuth.HANDLE";
    public static final String KEY_ENDPOINT = "YambaAuth.ENDPOINT";
    public static final String AUTH_TYPE_CLIENT = "YambaAuth.AUTH_CLIENT";

    public static Bundle buildAccountExtras(String handle, String endpoint) {
        Bundle acctExtras = new Bundle();
        acctExtras.putString(AccountMgr.KEY_HANDLE, handle);
        acctExtras.putString(AccountMgr.KEY_ENDPOINT, endpoint);
        return acctExtras;
    }

    private final YambaApplication app;

    public AccountMgr(YambaApplication app) {
        super(app);
        this.app = app;
    }

    @Override
    public Bundle addAccount(
        AccountAuthenticatorResponse resp,
        String accountType,
        String authTokenType,
        String[] requiredFeatures,
        Bundle options)
        throws NetworkErrorException
    {
        if (BuildConfig.DEBUG) { Log.d(TAG, "addAccount"); }
        Bundle reply = new Bundle();

        String at = app.getString(R.string.account_type);

        if (!at.equals(accountType)) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(
                AccountManager.KEY_ERROR_MESSAGE,
                "Unrecognized account type");
            return reply;
        }

        if (0 < AccountManager.get(app).getAccountsByType(at).length) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(
                AccountManager.KEY_ERROR_MESSAGE,
                "Account already exists: please delete before creating a new one");
            return reply;
        }

        Intent intent = new Intent(app, NewAccountActivity.class);
        reply.putParcelable(AccountManager.KEY_INTENT, intent);

        return reply;
    }

    @Override
    public Bundle getAuthToken(
        AccountAuthenticatorResponse response,
        Account account,
        String authTokenType,
        Bundle options)
    {
        Bundle reply = new Bundle();
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        reply.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);

        if (!app.getString(R.string.account_type).equals(account.type)) {
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, "Unrecognized account type");
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            return reply;
        }

        if (!AUTH_TYPE_CLIENT.equals(authTokenType)) {
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, "Unrecognized authentication type");
            reply.putInt(AccountManager.KEY_ERROR_CODE, -2);
            Log.d(TAG, "unrecognized auth type: " + authTokenType);
            return reply;
        }

        AccountManager mgr = AccountManager.get(app);
        String token = UUID.randomUUID().toString();
        app.createClient(
            token,
            mgr.getUserData(account, AccountMgr.KEY_HANDLE),
            mgr.getPassword(account),
            mgr.getUserData(account, AccountMgr.KEY_ENDPOINT));

        reply.putString(AccountManager.KEY_AUTHTOKEN, token);

        return reply;
    }

    @Override
    public Bundle editProperties(
        AccountAuthenticatorResponse accountAuthenticatorResponse,
        String s)
    {
        throw new UnsupportedOperationException("editProperties not supported.");
    }

    @Override
    public Bundle confirmCredentials(
        AccountAuthenticatorResponse accountAuthenticatorResponse,
        Account account,
        Bundle bundle)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException("confirmCredentials not supported.");
    }

    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException("getAuthTokenLabel not supported.");
    }

    @Override
    public Bundle updateCredentials(
        AccountAuthenticatorResponse accountAuthenticatorResponse,
        Account account,
        String s,
        Bundle bundle)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException("updateCredentials not supported.");
    }

    @Override
    public Bundle hasFeatures(
        AccountAuthenticatorResponse accountAuthenticatorResponse,
        Account account,
        String[] strings)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }
}
