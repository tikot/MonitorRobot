package co.rytikov.monitorrobot.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.rytikov.monitorrobot.R;
import co.rytikov.monitorrobot.Utility;
import co.rytikov.monitorrobot.data.RobotContract.AccountEntry;
import co.rytikov.monitorrobot.data.RobotContract.MonitorEntry;
import co.rytikov.monitorrobot.endpoint.UptimeClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewMonitor extends DialogFragment implements AdapterView.OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener, TextWatcher, Callback<UptimeClient.CallResult> {

    private static String LOG_TAG = AddNewMonitor.class.getSimpleName();

    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.friendly_name) TextInputEditText friendlyName;
    @BindView(R.id.url) TextInputEditText url;
    @BindView(R.id.url_layout) TextInputLayout url_layout;
    @BindView(R.id.port) TextInputEditText port;
    @BindView(R.id.port_layout) TextInputLayout port_layout;
    @BindView(R.id.interval_minutes) TextInputEditText interval_minutes;
    @BindView(R.id.interval) SeekBar interval;

    private String monitorFriendlyName;
    private String monitorURL;
    private String monitorType;
    private String monitorPort;
    private String monitorInterval;

    private int monitoringInterval;
    private boolean seekBarUser = false;
    private static View parentView;
    private static String API_KEY;

    public static AddNewMonitor newInstance(View view, String api) {
        API_KEY = api;
        parentView = view;
        return new AddNewMonitor();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_new_monitor, null);
        ButterKnife.bind(this, view);

        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.monitor_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Cursor cursor = getContext().getContentResolver().query(
                AccountEntry.CONTENT_URI, AccountEntry.PROJECTION,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            monitoringInterval = cursor.getInt(1);
            cursor.close();
        } else
            monitoringInterval = 5;

        interval_minutes.addTextChangedListener(this);
        interval.setOnSeekBarChangeListener(this);

        builder.setView(view)
                .setPositiveButton(R.string.create_monitor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        createMonitor();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddNewMonitor.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    /**
     * Add new monitor to UptimeRobot
     */
    public void createMonitor() {
        monitorFriendlyName = friendlyName.getText().toString();
        monitorURL = url.getText().toString();
        monitorType = spinner.getSelectedItem().toString();
        monitorPort = port.getText().toString();
        monitorInterval = interval_minutes.getText().toString();

        if (monitorURL.isEmpty()) {
            Snackbar.make(parentView, "Fail adding a monitor needs URL or IP", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if (monitorType.equals("HTTP(s)")) {
            if (!URLUtil.isHttpUrl(monitorURL) || !URLUtil.isHttpsUrl(monitorURL)) {
                monitorURL = "http://" + monitorURL;
            }
        }

        if (monitorFriendlyName.isEmpty()) {
            monitorFriendlyName = monitorURL;
        }

        String mPort = null;
        String subType = null;
        if (!monitorPort.isEmpty()) {
            mPort = monitorPort;
            subType = Utility.getSubTypeID(monitorPort);
        }

        monitorType = Utility.getTypeID(monitorType);

        UptimeClient.UptimeRobot service = UptimeClient.retrofit
                .create(UptimeClient.UptimeRobot.class);

        Call<UptimeClient.CallResult> call = service.newMonitor(
                API_KEY, monitorFriendlyName, monitorURL, monitorType,
                subType, mPort, monitorInterval);

        call.enqueue(this);
    }

    /**
     * After the API call save to DB
     * @param id int
     */
    public void saveMonitor(int id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MonitorEntry.COLUMN_MONITOR_ID, id);
        contentValues.put(MonitorEntry.COLUMN_FRIENDLY_NAME, monitorFriendlyName);
        contentValues.put(MonitorEntry.COLUMN_URL, monitorURL);
        contentValues.put(MonitorEntry.COLUMN_TYPE, monitorType);
        contentValues.put(MonitorEntry.COLUMN_PORT, monitorPort);
        contentValues.put(MonitorEntry.COLUMN_INTERVAL, monitorInterval);

        parentView.getContext().getContentResolver().insert(
                MonitorEntry.CONTENT_URI, contentValues);

        Snackbar.make(parentView, "Adding new monitor...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * OnItemSelectedListener
     */

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Resources resources = getContext().getResources();
        switch (i) {
            case 0:
                url_layout.setHint(resources.getString(R.string.url_or_ip));
                port_layout.setVisibility(View.GONE);
                break;
            case 1:
                url_layout.setHint(resources.getString(R.string.ip_or_host));
                port_layout.setVisibility(View.GONE);
                break;
            case 2:
                url_layout.setHint(resources.getString(R.string.ip_or_url_or_host));
                port_layout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * OnSeekBarChangeListener
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (seekBarUser) {
            if (progress < monitoringInterval)
                interval_minutes.setText(String.valueOf(monitoringInterval));
            else interval_minutes.setText(String.valueOf(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBarUser = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBarUser = false;
    }

    /**
     * TextWatcher
     */

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (!seekBarUser && !editable.toString().equals("")) {
            int n = Integer.parseInt(editable.toString());

            if (n < monitoringInterval) {
                interval_minutes.setText(String.valueOf(monitoringInterval));
            }

            if (n >= 120) interval.setProgress(120);
            else interval.setProgress(n);
        }
    }

    /**
     * new Callback<UptimeClient.CallResult>()
     */

    @Override
    public void onResponse(Call<UptimeClient.CallResult> call, Response<UptimeClient.CallResult> response) {
        saveMonitor(response.body().monitor.id);
    }

    @Override
    public void onFailure(Call<UptimeClient.CallResult> call, Throwable t) {
        Log.w(LOG_TAG, "Failure API check " + t.getMessage());
    }
}
