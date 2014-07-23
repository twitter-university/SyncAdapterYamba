package com.twitter.university.android.yamba.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class SyncService extends Service {
    private static final String TAG = "SYNC_SVC";


    private volatile SyncAdapter synchronizer;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronizer = new SyncAdapter(getApplication(), true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return synchronizer.getSyncAdapterBinder();
    }
}
