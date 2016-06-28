package co.rytikov.monitorrobot.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RobotProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private RobotSQLiteHelper mOpenHelper;

    static final int CODE_ACCOUNT = 100;
    static final int CODE_MONITOR = 200;
    static final int CODE_MONITOR_ID = 250;
    static final int CODE_MONITOR_LOGS = 300;
    static final int CODE_RESPONSE_TIME = 400;

    static {
        final String authority = RobotContract.CONTENT_AUTHORITY;
        sUriMatcher.addURI(authority, RobotContract.PATH_ACCOUNT, CODE_ACCOUNT);
        sUriMatcher.addURI(authority, RobotContract.PATH_MONITOR, CODE_MONITOR);
        sUriMatcher.addURI(authority, RobotContract.PATH_MONITOR + "/*", CODE_MONITOR_ID);
        sUriMatcher.addURI(authority, RobotContract.PATH_LOG + "/*", CODE_MONITOR_LOGS);
        sUriMatcher.addURI(authority, RobotContract.PATH_RESPONSE_TIME + "/*", CODE_RESPONSE_TIME);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RobotSQLiteHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_ACCOUNT:
                cursor = db.query(
                        RobotContract.AccountEntry.TABLE_NAME,
                        new String[] {"*"}, null, null, null, null, null
                );
                break;
            case CODE_MONITOR:
                    cursor = db.query(
                        RobotContract.MonitorEntry.TABLE_NAME,
                        new String[] {"*"}, null, null, null, null, null);
                break;
            case CODE_MONITOR_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query
                        (RobotContract.MonitorEntry.TABLE_NAME,
                        new String[] {"*"},
                        RobotContract.MonitorEntry.COLUMN_MONITOR_ID + " = " + id,
                        null, null, null, null);
                break;
            case CODE_MONITOR_LOGS:
                cursor = db.query(
                        RobotContract.LogEntry.TABLE_NAME,
                        new String[] {"*"}, null, null, null, null, null
                );
                break;
            case CODE_RESPONSE_TIME:
                cursor = db.query(
                        RobotContract.ResponseEntry.TABLE_NAME,
                        new String[] {"*"}, null, null, null, null, null
                );
                break;
            default:
                throw new UnsupportedOperationException("Query unknown uri: " + uri);
        }

        if (cursor != null) {
            Context ctx = getContext();
            assert ctx != null;
            cursor.setNotificationUri(ctx.getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_ACCOUNT:
                return RobotContract.AccountEntry.CONTENT_ITEM_TYPE;
            case CODE_MONITOR:
                return RobotContract.MonitorEntry.CONTENT_TYPE;
            case CODE_MONITOR_ID:
                return RobotContract.MonitorEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Get type unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri returnUri;
        final long _id;

        switch (sUriMatcher.match(uri)) {
            case CODE_ACCOUNT:
                _id = db.insertOrThrow(RobotContract.AccountEntry.TABLE_NAME, null, contentValues);
                returnUri = RobotContract.AccountEntry.buildAccountUri(_id);
                break;
            case CODE_MONITOR:
                _id = db.insertOrThrow(RobotContract.MonitorEntry.TABLE_NAME, null, contentValues);
                returnUri = RobotContract.MonitorEntry.buildMonitorUri(_id);
                break ;
            case CODE_MONITOR_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Insert unknown uri: " + uri);
        }

        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        if ( null == selection ) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_ACCOUNT:
                rowsDeleted = db.delete(
                        RobotContract.AccountEntry.TABLE_NAME,
                        null, null);
                break;
            case CODE_MONITOR_ID:
                String id = uri.getLastPathSegment();
                rowsDeleted = db.delete(
                        RobotContract.MonitorEntry.TABLE_NAME,
                        RobotContract.MonitorEntry.COLUMN_MONITOR_ID + " = " + id, null);
                break;
            default:
                throw new UnsupportedOperationException("Delete unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rowsUpdated = 0;
        switch (sUriMatcher.match(uri)) {
            case CODE_ACCOUNT:
                // update one first row, since we only have one row.
                rowsUpdated = db.update(
                        RobotContract.AccountEntry.TABLE_NAME,
                        values, "1", null);
                break;
            case CODE_MONITOR_ID:
                String id = uri.getLastPathSegment();
                rowsUpdated = db.update(RobotContract.MonitorEntry.TABLE_NAME,
                        values, RobotContract.MonitorEntry.COLUMN_MONITOR_ID + " =" + id, null);
                break;
            default:
                throw new UnsupportedOperationException("Update unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
