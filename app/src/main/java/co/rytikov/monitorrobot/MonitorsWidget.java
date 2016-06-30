package co.rytikov.monitorrobot;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;

import co.rytikov.monitorrobot.data.RobotContract;

/**
 * Implementation of App Widget functionality.
 */
public class MonitorsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.monitors_widget);

        Cursor cursor = context.getContentResolver().query(
                RobotContract.AccountEntry.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            views.setTextViewText(R.id.up_number, cursor.getString(2));
            views.setTextViewText(R.id.down_number, cursor.getString(3));
            cursor.close();
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

