package co.rytikov.monitorrobot.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import co.rytikov.monitorrobot.accounts.AuthenticatorService;
import co.rytikov.monitorrobot.data.RobotContract;

public class SyncUtils {
    private static final String CONTENT_AUTHORITY = RobotContract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        Account account = AuthenticatorService.getSyncAccount();
        AccountManager accountManager = (AccountManager)
                context.getSystemService(Context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(), getSyncFrequency(context));

            newAccount = true;
        }
        if (newAccount || !setupComplete) {
            triggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    public static void triggerRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(AuthenticatorService.getSyncAccount(),
                CONTENT_AUTHORITY, bundle);
    }

    public static long getSyncFrequency(Context context) {
        String frequency = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sync_frequency", "180");
        switch (frequency) {
            case "60":
                return 60 * 60;
            default:
            case "180":
                return 60 * 60 * 3;
            case "360":
                return 60 * 60 * 6;
        }
    }
}
