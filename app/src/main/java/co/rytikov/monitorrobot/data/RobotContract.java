package co.rytikov.monitorrobot.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class RobotContract {
    public static final String CONTENT_AUTHORITY = "co.rytikov.monitorrobot";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ACCOUNT = "account";
    public static final String PATH_MONITOR = "monitor";
    public static final String PATH_LOG = "log";
    public static final String PATH_RESPONSE_TIME = "response_time";

    /**
     * Account Columns
     */
    public static final class AccountEntry implements BaseColumns {

        public static final String TABLE_NAME = "account";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACCOUNT).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACCOUNT;

        //Columns
        public static final String COLUMN_MONITOR_LIMIT = "monitor_limit";
        public static final String COLUMN_MONITOR_INTERVAL = "monitor_interval";
        public static final String COLUMN_UP_MONITORS = "up_monitors";
        public static final String COLUMN_DOWN_MONITORS = "down_monitors";
        public static final String COLUMN_PAUSED_MONITORS = "paused_monitors";

        public static final String[] PROJECTION = new String[] {
                COLUMN_MONITOR_LIMIT,
                COLUMN_MONITOR_INTERVAL,
                COLUMN_UP_MONITORS,
                COLUMN_DOWN_MONITORS,
                COLUMN_PAUSED_MONITORS
        };

        public static Uri buildAccountUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Monitor Columns
     */
    public static final class MonitorEntry implements BaseColumns {
        public static final String TABLE_NAME = "monitor";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MONITOR).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MONITOR;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MONITOR;

        public static final String COLUMN_MONITOR_ID = "monitor_id";
        public static final String COLUMN_FRIENDLY_NAME = "friendly_name";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SUBTYPE = "subtype";
        public static final String COLUMN_PORT = "port";
        public static final String COLUMN_INTERVAL = "interval";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_ALL_TIME_UPTIME_RATIO = "all_time_uptime_ratio";

        public static final String[] PROJECTION = new String[] {
                COLUMN_MONITOR_ID,
                COLUMN_FRIENDLY_NAME,
                COLUMN_URL,
                COLUMN_TYPE,
                COLUMN_SUBTYPE,
                COLUMN_PORT,
                COLUMN_INTERVAL,
                COLUMN_STATUS,
                COLUMN_ALL_TIME_UPTIME_RATIO
        };

        public static Uri buildMonitorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * LogEntry Columns
     */
    public static final class LogEntry implements BaseColumns {
        public static final String TABLE_NAME = "log";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOG;

        public static final String COLUMN_MONITOR_ID = "monitor_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATE_TIME = "datetime";

        public static final String[] PROJECTION = new String[] {
                COLUMN_MONITOR_ID,
                COLUMN_TYPE,
                COLUMN_DATE_TIME
        };

        public static Uri buildLogUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Response Time Entry
     */
    public static final class ResponseEntry implements BaseColumns {
        public static final String TABLE_NAME = "response";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RESPONSE_TIME).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_RESPONSE_TIME;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_RESPONSE_TIME;

        public static final String COLUMN_MONITOR_ID = "monitor_id";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_DATE_TIME = "datetime";

        public static final String[] PROJECTION = new String[] {
                COLUMN_MONITOR_ID,
                COLUMN_VALUE,
                COLUMN_DATE_TIME
        };

        public static Uri buildResponseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
