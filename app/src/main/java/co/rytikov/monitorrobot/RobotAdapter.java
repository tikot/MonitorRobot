package co.rytikov.monitorrobot;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RobotAdapter extends RecyclerView.Adapter<RobotAdapter.ViewHolder> {

    private Cursor mCursor;
    private Context context;

    public static final int MONITOR_ID = 0;
    public static final int NAME = 1;
    public static final int URL = 2;
    public static final int TYPE = 3;
    public static final int SUBTYPE =  4;
    public static final int PORT = 5;
    public static final int INTERVAL = 6;
    public static final int STATUS = 7;
    public static final int UP_RATIO = 8;

    public RobotAdapter(Context context) {
    }

    @Override
    public RobotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.monitor_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RobotAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String name = mCursor.getString(NAME);
        String  url = mCursor.getString(URL);
        String uptime_radio = mCursor.getString(UP_RATIO);
        int status = mCursor.getInt(STATUS);

        holder.name.setText(name);
        holder.url.setText(url);
        holder.up_radio.setText(Utility.getPercent(context, uptime_radio));
        holder.status.setBackgroundColor(Utility.getStatusColor(context, status));
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(MONITOR_ID);
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name) TextView name;
        @BindView(R.id.url) TextView url;
        @BindView(R.id.up_time_ratio) TextView up_radio;
        @BindView(R.id.status) RelativeLayout status;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
