package co.rytikov.monitorrobot.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.rytikov.monitorrobot.data.RobotContract;
import co.rytikov.monitorrobot.data.RobotContract.AccountEntry;
import co.rytikov.monitorrobot.data.RobotContract.MonitorEntry;
import co.rytikov.monitorrobot.data.RobotContract.LogEntry;
import co.rytikov.monitorrobot.data.RobotContract.ResponseEntry;
import co.rytikov.monitorrobot.endpoint.UptimeClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = SyncAdapter.class.getSimpleName();

    private static final String PREF_API_KEY = "uptime_api_key";

    public ContentResolver mContentResolver;
    private String apiKey;
    private UptimeClient.UptimeRobot serviceEndpoint;

    /**
     * Account Entry columns
     */
    private static final int MONITOR_LIMIT = 0;
    private static final int MONITOR_INTERVAL = 1;
    private static final int UP_MONITORS = 2;
    private static final int DOWN_MONITORS = 3;
    private static final int PAUSED_MONITORS = 4;

    /**
     * Monitor Entry columns
     */
    private static final int MONITOR_ID = 0;
    private static final int NAME = 1;
    private static final int URL = 2;
    private static final int TYPE = 3;
    private static final int SUBTYPE =  4;
    private static final int PORT = 5;
    private static final int INTERVAL = 6;
    private static final int STATUS = 7;
    private static final int UP_RATIO = 8;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mContentResolver = context.getContentResolver();
        apiKey = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_API_KEY, "");
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");

        serviceEndpoint = UptimeClient.retrofit.create(UptimeClient.UptimeRobot.class);

        startMonitorSync();
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
                Log.i(TAG, "Sync Account failure message:" + t.getMessage());
            }
        });
    }

    /**
     * Send an data request
     */
    public void startMonitorSync() {
        Call<UptimeClient.Monitors> call = serviceEndpoint.getMonitors(apiKey);
        call.enqueue(new Callback<UptimeClient.Monitors>() {
            @Override
            public void onResponse(Call<UptimeClient.Monitors> call, Response<UptimeClient.Monitors> response) {
                syncMonitors(response.body().getMonitors());
            }
            @Override
            public void onFailure(Call<UptimeClient.Monitors> call, Throwable t) {
                Log.i(TAG, "Sync Monitor failure message: " + t.getMessage());
            }
        });
    }

    /**
     * Compare entry and updated database
     * @param monitors Monitor
     */
    public void syncMonitors(List<UptimeClient.Monitor> monitors) {

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        HashMap<Integer, UptimeClient.Monitor> entryMap = new HashMap<>();
        for (UptimeClient.Monitor m: monitors) {
            entryMap.put(m.id, m);
        }

        Log.i(TAG, "Fetching local monitors entries for merge");
        final Cursor cursor = mContentResolver.query(
                MonitorEntry.CONTENT_URI,
                MonitorEntry.PROJECTION,
                null, null, null);
        assert cursor != null;
        Log.i(TAG, "Found " + cursor.getCount() + " local entries. Computing merge solution...");

        while (cursor.moveToNext()) {
            final int id = cursor.getInt(MONITOR_ID);
            final String name = cursor.getString(NAME);
            final String url = cursor.getString(URL);
            //final String type = cursor.getString(TYPE);
            //final int sub = cursor.getInt(SUBTYPE);
            //final int port = cursor.getInt(PORT);
            final int interval = cursor.getInt(INTERVAL);
            final int status = cursor.getInt(STATUS);
            final String up_ratio = cursor.getString(UP_RATIO);

            UptimeClient.Monitor match = entryMap.get(id);
            if (match != null) {
                entryMap.remove(id);
                if (!match.friendlyname.equals(name) || !match.url.equals(url) ||
                        match.interval != interval || match.status != status ||
                        !match.alltimeuptimeratio.equals(up_ratio)) {
                    batch.add(ContentProviderOperation.newUpdate(MonitorEntry.buildMonitorUri(id))
                            .withValue(MonitorEntry.COLUMN_FRIENDLY_NAME, match.friendlyname)
                            .withValue(MonitorEntry.COLUMN_URL, match.url)
                            .withValue(MonitorEntry.COLUMN_URL, match.url)
                            .withValue(MonitorEntry.COLUMN_STATUS, match.status)
                            .withValue(MonitorEntry.COLUMN_INTERVAL, match.interval)
                            .withValue(MonitorEntry.COLUMN_ALL_TIME_UPTIME_RATIO,
                                    match.alltimeuptimeratio)
                            .build());
                }
                else {
                    Log.i(TAG, "No action: " + id);
                }

                if (match.log.size() != 0) {
                    batch.add(ContentProviderOperation.newDelete(
                            LogEntry.buildLogUri(id)
                    ).build());
                    for (UptimeClient.MonitorLog log : match.log) {
                        batch.add(ContentProviderOperation.newInsert(LogEntry.buildLogUri(id))
                                .withValue(LogEntry.COLUMN_MONITOR_ID, match.id)
                                .withValue(LogEntry.COLUMN_DATE_TIME, log.datetime)
                                .withValue(LogEntry.COLUMN_TYPE, log.type)
                                .build());
                    }
                }

                if (match.responsetime != null) {
                    batch.add(ContentProviderOperation.newDelete(
                            ResponseEntry.buildResponseUri(id)
                    ).build());
                    for (UptimeClient.ResponseTime response : match.responsetime) {
                        batch.add(ContentProviderOperation.newInsert(
                                ResponseEntry.buildResponseUri(id))
                                .withValue(ResponseEntry.COLUMN_MONITOR_ID, match.id)
                                .withValue(ResponseEntry.COLUMN_DATE_TIME, response.datetime)
                                .withValue(ResponseEntry.COLUMN_VALUE, response.value)
                                .build());
                    }
                }
             }
            else {
                //delete entry from database entry doesn't exists
                batch.add(ContentProviderOperation.newDelete(
                        MonitorEntry.buildMonitorUri(id)
                ).build());
                batch.add(ContentProviderOperation.newDelete(
                        LogEntry.buildLogUri(id)
                ).build());
                batch.add(ContentProviderOperation.newDelete(
                        ResponseEntry.buildResponseUri(id)
                ).build());
            }
        }
        cursor.close();

        for (UptimeClient.Monitor m : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + m.id);
            batch.add(ContentProviderOperation.newInsert(MonitorEntry.CONTENT_URI)
                    .withValue(MonitorEntry.COLUMN_MONITOR_ID, m.id)
                    .withValue(MonitorEntry.COLUMN_FRIENDLY_NAME, m.friendlyname)
                    .withValue(MonitorEntry.COLUMN_URL, m.url)
                    .withValue(MonitorEntry.COLUMN_TYPE, m.type)
                    .withValue(MonitorEntry.COLUMN_SUBTYPE, m.subtype)
                    .withValue(MonitorEntry.COLUMN_PORT, m.port)
                    .withValue(MonitorEntry.COLUMN_INTERVAL, m.interval)
                    .withValue(MonitorEntry.COLUMN_STATUS, m.status)
                    .withValue(MonitorEntry.COLUMN_ALL_TIME_UPTIME_RATIO, m.alltimeuptimeratio)
                    .build());
        }

        Log.i(TAG, "Merge solution ready. Applying batch update");
        try {
            mContentResolver.applyBatch(RobotContract.CONTENT_AUTHORITY, batch);
            mContentResolver.notifyChange(MonitorEntry.CONTENT_URI, null, false);
        } catch (RemoteException | OperationApplicationException e) {
            Log.i(TAG, "Merge solution failed!");
            e.printStackTrace();
        }
    }

}