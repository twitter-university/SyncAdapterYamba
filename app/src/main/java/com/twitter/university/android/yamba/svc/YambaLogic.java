package com.twitter.university.android.yamba.svc;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;
import com.twitter.university.android.yamba.BuildConfig;
import com.twitter.university.android.yamba.R;
import com.twitter.university.android.yamba.YambaContract;
import com.twitter.university.android.yamba.data.YambaProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class YambaLogic {
    private static final String TAG = "LOGIC";

    private final Context ctxt;
    private final int maxPolls;

    public YambaLogic(Context ctxt) {
        this.ctxt = ctxt;
        this.maxPolls = ctxt.getResources().getInteger(R.integer.poll_max);
    }

    public void doPost(String tweet) {
        ContentValues cv = new ContentValues();
        cv.put(YambaContract.Posts.Columns.TWEET, tweet);
        cv.put(YambaContract.Posts.Columns.TIMESTAMP, System.currentTimeMillis());
        ctxt.getContentResolver().insert(YambaContract.Posts.URI, cv);
    }

    public void doSync(YambaClient client) {
        Log.d(TAG, "sync");

        try { postPending(client); }
        catch (Exception e) {
            Log.e(TAG, "Post failed: " + e, e);
        }

        try { parseTimeline(client.getTimeline(maxPolls)); }
        catch (Exception e) {
            Log.e(TAG, "Poll failed: " + e, e);
        }
    }

    private int parseTimeline(List<Status> timeline) {
        long latest = getMaxTimestamp();

        List<ContentValues> vals = new ArrayList<ContentValues>();
        for (Status tweet: timeline) {
            long t = tweet.getCreatedAt().getTime();
            if (t <= latest) { continue; }

            ContentValues row = new ContentValues();
            row.put(YambaContract.Timeline.Columns.ID, Long.valueOf(tweet.getId()));
            row.put(YambaContract.Timeline.Columns.TIMESTAMP, Long.valueOf(t));
            row.put(YambaContract.Timeline.Columns.HANDLE, tweet.getUser());
            row.put(YambaContract.Timeline.Columns.TWEET, tweet.getMessage());
            vals.add(row);
        }

        int n = vals.size();
        if (0 >= n) { return 0; }
        n = ctxt.getContentResolver().bulkInsert(
            YambaContract.Timeline.URI,
            vals.toArray(new ContentValues[n]));

        if (BuildConfig.DEBUG) { Log.d(TAG, "inserted: " + n); }
        return n;
    }

    private long getMaxTimestamp() {
        Cursor c = null;
        try {
            c = ctxt.getContentResolver().query(
                YambaContract.MaxTimeline.URI,
                null,
                null,
                null,
                null);
            return ((null == c) || (!c.moveToNext()))
                ? Long.MIN_VALUE
                : c.getLong(0);
        }
        finally {
            if (null != c) { c.close(); }
        }
    }

    private int postPending(YambaClient client) throws YambaClientException {
        ContentResolver cr = ctxt.getContentResolver();
        String xactId = UUID.randomUUID().toString();

        int n = beginUpdate(cr, xactId);
        if (0 >= n) { return 0; }

        List<String> posted = new ArrayList<String>();
        Cursor cur = null;
        try {
            cur = cr.query(
                YambaContract.Posts.URI,
                null,
                YambaContract.Posts.Columns.TRANSACTION + "=?",
                new String[]{xactId},
                YambaContract.Posts.Columns.TIMESTAMP + " ASC");
            postTweets(client, cur, posted);
        }
        finally {
            if (null != cur) {
                try { cur.close(); } catch (Exception e) { }
                try { updateSucceeded(cr, posted); }
                finally { endUpdate(cr, xactId); }
            }
        }

        return posted.size();
    }

    private int beginUpdate(ContentResolver cr, String xactId) {
        ContentValues row = new ContentValues();
        row.put(YambaContract.Posts.Columns.TRANSACTION, xactId);
        int n = cr.update(
            YambaContract.Posts.URI,
            row,
            YambaProvider.CONSTRAINT_NEEDS_SYNC,
            null);
        if (BuildConfig.DEBUG) { Log.d(TAG, "begin update: " + n); }
        return n;
    }

    private void updateSucceeded(ContentResolver cr, List<String> posted) {
        int n = posted.size();
        if (BuildConfig.DEBUG) { Log.d(TAG, "update succeeded: " + n); }
        if (0 >= n) { return; }

        ContentValues row = new ContentValues();
        row.put(YambaContract.Posts.Columns.SENT, System.currentTimeMillis());
        cr.update(
            YambaContract.Posts.URI,
            row,
            YambaProvider.CONSTRAINT_IDS + "(" + TextUtils.join(",", posted) + ")",
            null);
    }

    private void endUpdate(ContentResolver cr, String xactId) {
        ContentValues row = new ContentValues();
        row.putNull(YambaContract.Posts.Columns.TRANSACTION);
        int n = cr.update(
            YambaContract.Posts.URI,
            row,
            YambaProvider.CONSTRAINT_XACT,
            new String[] { xactId });
        if (BuildConfig.DEBUG) { Log.d(TAG, "update complete: " + n); }
    }

    private void postTweets(YambaClient client, Cursor c, List<String> posted)
        throws YambaClientException
    {
        int idIdx = c.getColumnIndex(YambaContract.Posts.Columns.ID);
        int tweetIdx = c.getColumnIndex(YambaContract.Posts.Columns.TWEET);
        int n = 0;
        ContentValues row = new ContentValues();
        while (c.moveToNext()) {
            String tweet = c.getString(tweetIdx);
            // failure here will abort subsequent posts
            // and post order will be retained.
            client.postStatus(tweet);
            if (BuildConfig.DEBUG) { Log.d(TAG, "posted: " + tweet); }
            posted.add(c.getString(idIdx));
        }
    }
}
