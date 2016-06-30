package co.rytikov.monitorrobot.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.OnClick;
import co.rytikov.monitorrobot.R;
import co.rytikov.monitorrobot.RobotAdapter;
import co.rytikov.monitorrobot.data.RobotContract;
import co.rytikov.monitorrobot.sync.SyncUtils;

public class MainActivity extends TheActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>, RobotAdapter.RobotAdapterOnClick {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private static final String PREF_API_KEY = "uptime_api_key";
    private static String apiKey;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    private RobotAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isInitialized()) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RobotAdapter(getBaseContext(), this);
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        SyncUtils.createSyncAccount(this);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncUtils.triggerRefresh();
                mRefreshLayout.setRefreshing(false);
            }
        });

        if (findViewById(R.id.monitor_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }
    public boolean isInitialized() {
        apiKey = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREF_API_KEY, "");

        return !apiKey.equals("");
    }

    @OnClick(R.id.fab)
    public void fabOnClick(final View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddNewMonitor addMonitor = AddNewMonitor.newInstance(view, apiKey);
        addMonitor.show(fragmentManager, "monitor");
    }

    @Override
    public void onMonitorClick(View view, long monitorID, String monitorName) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putLong(MonitorDetailFragment.ARG_ITEM_ID, monitorID);
            arguments.putString(MonitorDetailFragment.ARG_ITEM_NAME, monitorName);
            arguments.putBoolean(MonitorDetailFragment.TWO_PANE, mTwoPane);
            MonitorDetailFragment fragment = new MonitorDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.monitor_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, MonitorDetailActivity.class);
            intent.putExtra(MonitorDetailFragment.ARG_ITEM_ID, monitorID);
            intent.putExtra(MonitorDetailFragment.ARG_ITEM_NAME, monitorName);
            intent.putExtra(MonitorDetailFragment.TWO_PANE, mTwoPane);

            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                RobotContract.MonitorEntry.CONTENT_URI,
                RobotContract.MonitorEntry.PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //mAdapter.setHasStableIds(true);
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        mRecyclerView.setAdapter(null);
    }
}
