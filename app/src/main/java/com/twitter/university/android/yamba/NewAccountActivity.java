package com.twitter.university.android.yamba;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.twitter.university.android.yamba.sync.AccountMgr;


public class NewAccountActivity extends AccountAuthenticatorActivity {
    private static final String TAG = "ACCOUNT";

    private static final String DEF_HANDLE = "student";
    private static final String DEF_PASSWORD = "password";
    private static final String DEF_ENDPOINT = "http://yamba.marakana.com/api";

    private String accountType;
    private int pollInterval;
    private EditText handle;
    private EditText password;
    private EditText endpoint;
    private Button submit;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_new_account);

        accountType = getString(R.string.account_type);

        // could get this from the user...
        pollInterval = getResources().getInteger(R.integer.poll_interval) * 1000;

        handle = (EditText) findViewById(R.id.account_handle);
        password = (EditText) findViewById(R.id.account_password);
        endpoint = (EditText) findViewById(R.id.account_endpoint);

        submit = (Button) findViewById(R.id.account_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { newAccount(); }
        });
    }

    private void newAccount() {
        createAccount(
            handle.getText().toString(),
            password.getText().toString(),
            endpoint.getText().toString());

        finish();
    }

    private void createAccount(
        String handle,
        String password,
        String endpoint)
    {
        if (TextUtils.isEmpty(handle)) { handle = DEF_HANDLE; }
        if (TextUtils.isEmpty(password)) { password = DEF_PASSWORD; }
        if (TextUtils.isEmpty(endpoint)) { endpoint = DEF_ENDPOINT; }

        String acctName = new StringBuilder().append(handle).append("@").append(endpoint).toString();
        if (BuildConfig.DEBUG) {
            Log.d( TAG, "create account: " + accountType + ": " + acctName);
        }

        Account account = new Account(acctName, accountType);
        Bundle acctExtras = AccountMgr.buildAccountExtras(handle, endpoint);
        if (!AccountManager.get(this).addAccountExplicitly(account, password, acctExtras)) {
            Toast.makeText(this, R.string.account_failed, Toast.LENGTH_LONG).show();
            return;
        }

        ContentResolver.setIsSyncable(account, YambaContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, YambaContract.AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, YambaContract.AUTHORITY, new Bundle(), pollInterval);
    }
}


