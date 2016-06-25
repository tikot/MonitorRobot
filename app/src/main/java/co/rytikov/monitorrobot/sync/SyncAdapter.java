package co.rytikov.monitorrobot.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import co.rytikov.monitorrobot.data.RobotContract.AccountEntry;
import co.rytikov.monitorrobot.endpoint.UptimeClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = SyncAdapter.class.getSimpleName();

    private static final String PREF_NAME = "co.rytikov.monitorrobot";
    private static final String PREF_API_KEY = "UPTIME_API_KEY";

    public ContentResolver mContentResolver;
    private String apiKey;
    private UptimeClient.UptimeRobot serviceEndpoint;

    /**
     * Account Entry columns
     */
    int MONITOR_LIMIT = 0;
    int MONITOR_INTERVAL = 1;
    int UP_MONITORS = 2;
    int DOWN_MONITORS = 3;
    int PAUSED_MONITORS = 4;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mContentResolver = context.getContentResolver();
        apiKey = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_API_KEY, "");
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");

        serviceEndpoint = UptimeClient.retrofit.create(UptimeClient.UptimeRobot.class);

        syncAccountData();
    }

    public void syncAccountData() {
        Log.i(TAG, "Fetching local entries for merge");
        final Cursor cursor = mContentResolver.query(
                AccountEntry.CONTENT_URI,
                AccountEntry.PROJECTION,
                null, null, null);
        assert cursor != null;
        Log.i(TAG, "Found " + cursor.getCount() + " local entries. Computing merge solution...");
        cursor.moveToFirst();

        final int limit = cursor.getInt(MONITOR_LIMIT);
        final int interval = cursor.getInt(MONITOR_INTERVAL);
        final int up = cursor.getInt(UP_MONITORS);
        final int down = cursor.getInt(DOWN_MONITORS);
        final int paused = cursor.getInt(PAUSED_MONITORS);

        cursor.close();

        Call<UptimeClient.AccountDetails> call = serviceEndpoint.getAccountDetails(apiKey);
        call.enqueue(new Callback<UptimeClient.AccountDetails>() {
            @Override
            public void onResponse(Call<UptimeClient.AccountDetails> call, Response<UptimeClient.AccountDetails> response) {

                UptimeClient.Account account = response.body().account;
                if (account.monitorLimit != limit || account.monitorInterval != interval ||
                        account.upMonitors != up || account.downMonitors != down ||
                        account.pausedMonitors != paused) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AccountEntry.COLUMN_MONITOR_LIMIT, account.monitorLimit);
                    contentValues.put(AccountEntry.COLUMN_MONITOR_INTERVAL, account.monitorInterval);
                    contentValues.put(AccountEntry.COLUMN_UP_MONITORS, account.upMonitors);
                    contentValues.put(AccountEntry.COLUMN_DOWN_MONITORS, account.downMonitors);
                    contentValues.put(AccountEntry.COLUMN_PAUSED_MONITORS, account.pausedMonitors);
                    int database = mContentResolver.update(AccountEntry.CONTENT_URI,
                            contentValues, null, null);

                    Log.i(TAG, "Sync database updated: " + database);
                }
            }

            @Override
            public void onFailure(Call<UptimeClient.AccountDetails> call, Throwable t) {
                Log.i(TAG, "Sync failure message:" + t.getMessage());
            }
        });
    }
}