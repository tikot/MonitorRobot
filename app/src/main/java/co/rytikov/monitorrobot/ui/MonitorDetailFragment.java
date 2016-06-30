package co.rytikov.monitorrobot.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.rytikov.monitorrobot.LogAdapter;
import co.rytikov.monitorrobot.R;
import co.rytikov.monitorrobot.data.RobotContract;
import co.rytikov.monitorrobot.endpoint.UptimeClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitorDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MonitorDetailFragment.class.getSimpleName();
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "monitor_id";
    public static final String ARG_ITEM_NAME = "monitor_name";
    private static long monitorId;
    private static String monitorName;
    public static final String TWO_PANE = "two_pane";
    private boolean mTwoPane;

    @BindView(R.id.no_data) TextView noData;
    @BindView(R.id.chart_card) CardView mCardView;
    @BindView(R.id.response_chart) LineChartView mChart;
    @BindView(R.id.log_recycler_view) RecyclerView logView;

    private String[] mChartLabels;
    private float[] mChartValues;

    private LogAdapter mAdapter;
    private static final int LOADER_ID_CURSOR_LOG = 1;
    private static final int LOADER_ID_CURSOR_RESPONSE = 2;
    private Cursor cursorResponse = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MonitorDetailFragment() {}

    final static String CHART_LABELS = "saveLabels";
    final static String CHART_VALUES = "saveValues";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(CHART_LABELS, mChartLabels);
        outState.putFloatArray(CHART_VALUES, mChartValues);
        outState.putLong(ARG_ITEM_ID, monitorId);
        outState.putString(ARG_ITEM_NAME, monitorName);
        outState.putBoolean(TWO_PANE, mTwoPane);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ITEM_ID) &&
                getArguments().containsKey(ARG_ITEM_NAME)) {
            monitorId = getArguments().getLong(ARG_ITEM_ID);
            monitorName = getArguments().getString(ARG_ITEM_NAME);
            mTwoPane = getArguments().getBoolean(TWO_PANE);
            appBarTitle();
        }

        if (savedInstanceState != null) {
            mChartLabels = savedInstanceState.getStringArray(CHART_LABELS);
            mChartValues = savedInstanceState.getFloatArray(CHART_VALUES);
            monitorId = savedInstanceState.getLong(ARG_ITEM_ID);
            monitorName = savedInstanceState.getString(ARG_ITEM_NAME);
            mTwoPane = savedInstanceState.getBoolean(TWO_PANE);
            appBarTitle();
        }
    }

    public void appBarTitle() {
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout =
                (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(monitorName);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.monitor_detail, container, false);
        ButterKnife.bind(this, rootView);

        logView.setHasFixedSize(true);
        logView.setNestedScrollingEnabled(false);
        logView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new LogAdapter();
        logView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //fix issue of loader starting twice
        if (getLoaderManager().getLoader(LOADER_ID_CURSOR_LOG) == null ||
                getLoaderManager().getLoader(LOADER_ID_CURSOR_RESPONSE) == null) {
            getLoaderManager().initLoader(LOADER_ID_CURSOR_LOG, null, this);
            getLoaderManager().initLoader(LOADER_ID_CURSOR_RESPONSE, null, this);
        }
        else {
            getLoaderManager().restartLoader(LOADER_ID_CURSOR_LOG, null, this);
            getLoaderManager().restartLoader(LOADER_ID_CURSOR_RESPONSE, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.monitor_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_monitor) {
            deleteMonitor();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteMonitor() {
        String apiKey = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("uptime_api_key", "");
        UptimeClient.UptimeRobot service = UptimeClient.retrofit
                .create(UptimeClient.UptimeRobot.class);
        Call<UptimeClient.CallResult> call = service.deleteMonitor(apiKey, Long.toString(monitorId));
        call.enqueue(new Callback<UptimeClient.CallResult>() {
            @Override
            public void onResponse(Call<UptimeClient.CallResult> call, Response<UptimeClient.CallResult> response) {
                if (response.body().stat.equals("fail")) return;
                getContext().getContentResolver().delete(
                        RobotContract.MonitorEntry.buildMonitorUri(monitorId), null, null);
                finishFragment();
            }

            @Override
            public void onFailure(Call<UptimeClient.CallResult> call, Throwable t) {
                Log.w(TAG, "onFailure delete monitor: " + t.getMessage());
            }
        });
    }

    private void finishFragment() {
        if (mTwoPane) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(MonitorDetailFragment.this).commit();
        }
        else {
            getActivity().finish();
        }
    }

    /**
     * organize data from cursor for the chart
     */
    private void setDataSet() {
        if (cursorResponse == null || cursorResponse.getCount() == 0) {
            return;
        }
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat output = new SimpleDateFormat("HH:mm");

        int RESPONSE_VALUE = 1;
        int RESPONSE_DATE_TIME = 2;
        int count = cursorResponse.getCount();
        mChartLabels = new String[count];
        mChartValues = new float[count];

        int i = 0;
        while (cursorResponse.moveToNext()){
            int value = cursorResponse.getInt(RESPONSE_VALUE);
            mChartValues[i] = (float) value/100;
            String date_time = cursorResponse.getString(RESPONSE_DATE_TIME);
            Date parsed = null;
            try {
                parsed = format.parse(date_time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mChartLabels[i] = output.format(parsed);
            i++;
        }

        setUpChart();
    }

    /**
     * setting up the chart with data and color
     */
    private void setUpChart() {
        if (mChartLabels.length == 0 || mChartValues.length == 0) {
            mCardView.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
            return;
        }
        noData.setVisibility(View.GONE);
        mCardView.setVisibility(View.VISIBLE);

        LineSet dataSet = new LineSet(mChartLabels, mChartValues);
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.primaryLight))
                .setDotsColor(ContextCompat.getColor(getContext(), R.color.chart_dot))
                .setThickness(4);
        mChart.addData(dataSet);

        mChart.setBorderSpacing(Tools.fromDpToPx(15))
                .setAxisBorderValues(0, 20)
                .setYLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(getContext(), R.color.primary))
                .setXAxis(false)
                .setYAxis(false);
        mChart.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LOADER_ID_CURSOR_LOG:
                return new CursorLoader(getContext(),
                        RobotContract.LogEntry.buildLogUri(monitorId),
                        RobotContract.LogEntry.PROJECTION,
                        null, null, null);
            case LOADER_ID_CURSOR_RESPONSE:
                return new CursorLoader(getContext(),
                        RobotContract.ResponseEntry.buildResponseUri(monitorId),
                        RobotContract.ResponseEntry.PROJECTION,
                        null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch(loader.getId()) {
            case LOADER_ID_CURSOR_LOG:
                mAdapter.swapCursor(cursor);
                break;
            case LOADER_ID_CURSOR_RESPONSE:
                cursorResponse = cursor;
                setDataSet();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ID_CURSOR_LOG) {
            mAdapter.swapCursor(null);
            logView.setAdapter(null);
        }
    }
}
