package com.twitter.university.android.yamba.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.twitter.university.android.yamba.YambaContract;

import java.util.Map;


public class YambaProvider extends ContentProvider {
    public static final String CONSTRAINT_IDS = YambaDbHelper.COL_ID + " in ";
    public static final String CONSTRAINT_XACT = YambaDbHelper.COL_XACT + "=?";
    public static final String CONSTRAINT_NEEDS_SYNC
        = YambaDbHelper.COL_SENT + " is null and " + YambaDbHelper.COL_XACT + " is null";

    private static final int MAX_TIMELINE_ITEM_TYPE = 1;
    private static final int TIMELINE_ITEM_TYPE = 2;
    private static final int TIMELINE_DIR_TYPE = 3;
    private static final int POST_ITEM_TYPE = 4;
    private static final int POST_DIR_TYPE = 5;

    //  scheme                     authority                   path  [id]
    // content://com.twitter.university.android.yamba.timeline/timeline/7
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.MaxTimeline.TABLE,
            MAX_TIMELINE_ITEM_TYPE);
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.Timeline.TABLE + "/#",
            TIMELINE_ITEM_TYPE);
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.Timeline.TABLE,
            TIMELINE_DIR_TYPE);
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.Posts.TABLE + "/#",
            POST_ITEM_TYPE);
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.Posts.TABLE,
            POST_DIR_TYPE);
    }

    private static final Map<String, String> PROJ_MAP_MAX_TIMELINE = new ProjectionMap.Builder()
        .addColumn(
            YambaContract.MaxTimeline.Columns.TIMESTAMP,
            "max(" + YambaDbHelper.COL_TIMESTAMP + ")")
        .build()
        .getProjectionMap();

    private static final ColumnMap COL_MAP_TIMELINE = new ColumnMap.Builder()
        .addColumn(
            YambaContract.Timeline.Columns.ID,
            YambaDbHelper.COL_ID,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Timeline.Columns.TIMESTAMP,
            YambaDbHelper.COL_TIMESTAMP,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Timeline.Columns.HANDLE,
            YambaDbHelper.COL_HANDLE,
            ColumnMap.Type.STRING)
        .addColumn(
            YambaContract.Timeline.Columns.TWEET,
            YambaDbHelper.COL_TWEET,
            ColumnMap.Type.STRING)
        .build();

    private static final Map<String, String> PROJ_MAP_TIMELINE = new ProjectionMap.Builder()
        .addColumn(YambaContract.Timeline.Columns.ID, YambaDbHelper.COL_ID)
        .addColumn(YambaContract.Timeline.Columns.TIMESTAMP, YambaDbHelper.COL_TIMESTAMP)
        .addColumn(YambaContract.Timeline.Columns.HANDLE, YambaDbHelper.COL_HANDLE)
        .addColumn(YambaContract.Timeline.Columns.TWEET, YambaDbHelper.COL_TWEET)
        .build()
        .getProjectionMap();

    private static final ColumnMap COL_MAP_POSTS = new ColumnMap.Builder()
        .addColumn(
            YambaContract.Posts.Columns.ID,
            YambaDbHelper.COL_ID,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Posts.Columns.TIMESTAMP,
            YambaDbHelper.COL_TIMESTAMP,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Posts.Columns.TRANSACTION,
            YambaDbHelper.COL_XACT,
            ColumnMap.Type.STRING)
        .addColumn(
            YambaContract.Posts.Columns.SENT,
            YambaDbHelper.COL_SENT,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Posts.Columns.TWEET,
            YambaDbHelper.COL_TWEET,
            ColumnMap.Type.STRING)
        .build();

    private static final Map<String, String> PROJ_MAP_POSTS = new ProjectionMap.Builder()
        .addColumn(YambaContract.Posts.Columns.ID, YambaDbHelper.COL_ID)
        .addColumn(YambaContract.Posts.Columns.TIMESTAMP, YambaDbHelper.COL_TIMESTAMP)
        .addColumn(YambaContract.Posts.Columns.TRANSACTION, YambaDbHelper.COL_XACT)
        .addColumn(YambaContract.Posts.Columns.SENT, YambaDbHelper.COL_SENT)
        .addColumn(YambaContract.Posts.Columns.TWEET, YambaDbHelper.COL_TWEET)
        .build()
        .getProjectionMap();

    private YambaDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new YambaDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (MATCHER.match(uri)) {
            case MAX_TIMELINE_ITEM_TYPE:
                return YambaContract.MaxTimeline.ITEM_TYPE;
            case TIMELINE_ITEM_TYPE:
                return YambaContract.Timeline.ITEM_TYPE;
            case TIMELINE_DIR_TYPE:
                return YambaContract.Timeline.DIR_TYPE;
            case POST_ITEM_TYPE:
                return YambaContract.Posts.ITEM_TYPE;
            case POST_DIR_TYPE:
                return YambaContract.Posts.DIR_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs, String sort) {

        String table;
        long pk = -1;
        Map<String, String> projMap;
        switch (MATCHER.match(uri)) {
            case MAX_TIMELINE_ITEM_TYPE:
                projMap = PROJ_MAP_MAX_TIMELINE;
                table = YambaDbHelper.TABLE_TIMELINE;
                break;
            case TIMELINE_ITEM_TYPE:
                pk = ContentUris.parseId(uri);
            case TIMELINE_DIR_TYPE:
                projMap = PROJ_MAP_TIMELINE;
                table = YambaDbHelper.TABLE_TIMELINE;
                break;
            case POST_ITEM_TYPE:
                pk = ContentUris.parseId(uri);
            case POST_DIR_TYPE:
                table = YambaDbHelper.TABLE_POSTS;
                projMap = PROJ_MAP_POSTS;
                break;
            default:
                throw new IllegalArgumentException("Unexpected uri: " + uri);
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        qb.setProjectionMap(projMap);

        if (0 < pk) { qb.appendWhere(YambaDbHelper.COL_ID + "=" + pk); }

        Cursor c = qb.query(getDb(), proj, sel, selArgs, null, null, sort);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] rows) {
        switch (MATCHER.match(uri)) {
            case TIMELINE_DIR_TYPE:
                break;
            default:
                throw new IllegalArgumentException("Unrecognized uri: " + uri);
        }

        SQLiteDatabase db = getDb();
        int count = 0;
        try {
            db.beginTransaction();

            for (ContentValues row: rows) {
                row = COL_MAP_TIMELINE.translateCols(row);
                if (0 < db.insert(YambaDbHelper.TABLE_TIMELINE, null, row)) {
                    count++;
                }
            }

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        if (0 < count) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }

        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues row) {
        switch (MATCHER.match(uri)) {
            case POST_DIR_TYPE:
                break;
            default:
                throw new IllegalArgumentException("Unexpected uri: " + uri);
        }

        long id = getDb().insert(
            YambaDbHelper.TABLE_POSTS,
            null,
            COL_MAP_POSTS.translateCols(row));

        if (0 >= id) { return null; }

        uri = uri.buildUpon().appendPath(String.valueOf(id)).build();
        getContext().getContentResolver().notifyChange(uri, null, true);

        return uri;
    }

    @Override
    public int update(Uri uri, ContentValues row, String sel, String[] selArgs) {
        switch (MATCHER.match(uri)) {
            case POST_DIR_TYPE:
                break;
            default:
                throw new IllegalArgumentException("Unexpected uri: " + uri);
        }

        int n = getDb().update(
            YambaDbHelper.TABLE_POSTS,
            COL_MAP_POSTS.translateCols(row),
            sel,
            selArgs);

        if (0 < n) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }

        return n;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException("delete not supported");
    }

    private SQLiteDatabase getDb() { return dbHelper.getWritableDatabase(); }
}
