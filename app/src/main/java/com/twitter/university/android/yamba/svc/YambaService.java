package com.twitter.university.android.yamba.svc;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    private static final String PARAM_OP = "YambaService.OP";
    private static final int OP_POST = -2;

    private static final String PARAM_TWEET = "YambaService.TWEET";

    public static void postTweet(Context ctxt, String tweet) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_OP, OP_POST);
        i.putExtra(PARAM_TWEET, tweet);
        ctxt.startService(i);
    }

    private volatile YambaLogic helper;

    public YambaService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new YambaLogic(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int op = intent.getIntExtra(PARAM_OP, 0);
        switch(op) {
            case OP_POST:
                helper.doPost(intent.getStringExtra(PARAM_TWEET));
                break;

            default:
                throw new IllegalArgumentException("Unrecognized op: " + op);
        }
    }
}
