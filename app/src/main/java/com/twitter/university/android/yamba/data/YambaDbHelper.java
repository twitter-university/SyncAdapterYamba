package com.twitter.university.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class YambaDbHelper extends SQLiteOpenHelper {
    private static final String DB_FILE = "yamba.db";
    private static final int VERSION = 3;

    public static final String TABLE_TIMELINE = "p_timeline";
    public static final String COL_ID = "p_id";
    public static final String COL_TIMESTAMP = "p_timestamp";
    public static final String COL_HANDLE = "p_handle";
    public static final String COL_TWEET = "p_tweet";

    public static final String TABLE_POSTS = "p_posts";
    public static final String COL_XACT = "p_xact";
    public static final String COL_SENT = "p_sent";


    public YambaDbHelper(Context ctxt) {
        super(ctxt, DB_FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE_TIMELINE + " ("
                + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_TIMESTAMP + " INTEGER NOT NULL,"
                + COL_HANDLE + " TEXT NOT NULL,"
                + COL_TWEET + " TEXT NOT NULL)"
            );
        db.execSQL(
            "CREATE TABLE " + TABLE_POSTS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TIMESTAMP + " INTEGER NOT NULL,"
                + COL_XACT + " STRING DEFAULT(NULL),"
                + COL_SENT + " INTEGER DEFAULT(NULL),"
                + COL_TWEET + " STRING NOT NULL" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMELINE);
        onCreate(db);
    }
}
