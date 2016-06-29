package co.rytikov.monitorrobot.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import co.rytikov.monitorrobot.R;
import co.rytikov.monitorrobot.endpoint.UptimeClient;
import co.rytikov.monitorrobot.data.RobotContract.AccountEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupActivity extends TheActivity {

    private static final String LOG_TAG = SetupActivity.class.getSimpleName();
    private static final String PREF_API_KEY = "uptime_api_key";

    @BindView(R.id.api_key) TextView apiTextView;
    @BindString(R.string.api_required) String requiredError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }

    @OnClick(R.id.next)
    public void next(final View view) {
        final CharSequence api = apiTextView.getText();

        if (api.length() == 0) {
            apiTextView.setError(getString(R.string.api_required));
            return;
        }

        UptimeClient.UptimeRobot service = UptimeClient.retrofit
                .create(UptimeClient.UptimeRobot.class);
        Call<UptimeClient.AccountDetails> call = service.getAccountDetails(api.toString());

        call.enqueue(new Callback<UptimeClient.AccountDetails>() {
            @Override
            public void onResponse(Call<UptimeClient.AccountDetails> call, Response<UptimeClient.AccountDetails> response) {
                if (response.body().stat.equals("fail")) {
                    apiTextView.setError(getString(R.string.api_fail));
                    return;
                }

                PreferenceManager.getDefaultSharedPreferences(SetupActivity.this).edit()
                        .putString(PREF_API_KEY, api.toString()).commit();

                UptimeClient.Account account = response.body().account;
                ContentValues contentValues = new ContentValues();
                contentValues.put(AccountEntry.COLUMN_MONITOR_LIMIT, account.monitorLimit);
                contentValues.put(AccountEntry.COLUMN_MONITOR_INTERVAL, account.monitorInterval);
                contentValues.put(AccountEntry.COLUMN_UP_MONITORS, account.upMonitors);
                contentValues.put(AccountEntry.COLUMN_DOWN_MONITORS, account.downMonitors);
                contentValues.put(AccountEntry.COLUMN_PAUSED_MONITORS, account.pausedMonitors);

                getContentResolver().insert(AccountEntry.CONTENT_URI, contentValues);

                startMainActivity();
            }

            @Override
            public void onFailure(Call<UptimeClient.AccountDetails> call, Throwable t) {
                Snackbar.make(view, getString(R.string.no_connection), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.w(LOG_TAG, t.getMessage());
            }
        });
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
