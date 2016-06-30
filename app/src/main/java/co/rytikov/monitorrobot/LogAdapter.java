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

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    Cursor mCursor;
    private Context context;

    public static final int LOG_TYPE = 1;
    public static final int LOG_DATE = 2;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.monitor_log_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String logType = mCursor.getString(LOG_TYPE);
        String logDate = mCursor.getString(LOG_DATE);

        holder.logDate.setText(logDate);
        holder.logType.setText(Utility.getLogType(logType));
        holder.logColor.setBackgroundColor(Utility.getLogColor(context, logType));
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        @BindView(R.id.log_date) TextView logDate;
        @BindView(R.id.log_type) TextView logType;
        @BindView(R.id.log_type_color) RelativeLayout logColor;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }
    }
}
