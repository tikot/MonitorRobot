package co.rytikov.monitorrobot.data;

import android.content.ContentProvider;
import android.content.ContentValues;
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

    static {
        final String authority = RobotContract.CONTENT_AUTHORITY;
        sUriMatcher.addURI(authority, RobotContract.PATH_ACCOUNT, CODE_ACCOUNT);
        sUriMatcher.addURI(authority, RobotContract.PATH_MONITOR, CODE_MONITOR);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RobotSQLiteHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_ACCOUNT:
                cursor = mOpenHelper.getReadableDatabase().query(
                        RobotContract.AccountEntry.TABLE_NAME,
                        new String[] {"*"},
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            case CODE_MONITOR:
                cursor = mOpenHelper.getReadableDatabase().query(
                        RobotContract.AccountEntry.TABLE_NAME,
                        new String[] {"*"},
                        null,
                        null,
                        null,
                        null,
                        null);
                break;
            default:
                throw new UnsupportedOperationException("Query unknown uri: " + uri);
        }

        if (cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

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
            default:
                throw new UnsupportedOperationException("Insert unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
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
            default:
                throw new UnsupportedOperationException("Delete unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
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
            default:
                throw new UnsupportedOperationException("Update unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
