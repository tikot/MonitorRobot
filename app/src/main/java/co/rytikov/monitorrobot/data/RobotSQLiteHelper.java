package co.rytikov.monitorrobot.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.rytikov.monitorrobot.data.RobotContract.AccountEntry;
import co.rytikov.monitorrobot.data.RobotContract.MonitorEntry;
import co.rytikov.monitorrobot.data.RobotContract.ResponseEntry;
import co.rytikov.monitorrobot.data.RobotContract.LogEntry;

public class RobotSQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "monitorRobot.db";

    public RobotSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ACCOUNT_TABLE = "CREATE TABLE " +
                AccountEntry.TABLE_NAME + " (" +
                AccountEntry.COLUMN_MONITOR_LIMIT + " REAL NOT NULL, " +
                AccountEntry.COLUMN_MONITOR_INTERVAL + " REAL NOT NULL, " +
                AccountEntry.COLUMN_UP_MONITORS + " REAL NULL, " +
                AccountEntry.COLUMN_DOWN_MONITORS + " REAL NULL, " +
                AccountEntry.COLUMN_PAUSED_MONITORS + " REAL NULL" +
                " );";

        final String SQL_CREATE_MONITOR_TABLE = "CREATE TABLE " +
                MonitorEntry.TABLE_NAME + " (" +
                MonitorEntry.COLUMN_MONITOR_ID + " INTEGER NOT NULL, " +
                MonitorEntry.COLUMN_FRIENDLY_NAME + " TEXT NOT NULL, " +
                MonitorEntry.COLUMN_URL + " TEXT NOT NULL, " +
                MonitorEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                MonitorEntry.COLUMN_SUBTYPE + " REAL NULL, " +
                MonitorEntry.COLUMN_PORT + " REAL NULL, " +
                MonitorEntry.COLUMN_INTERVAL + " REAL NULL, " +
                MonitorEntry.COLUMN_STATUS + " INTEGER NULL, " +
                MonitorEntry.COLUMN_ALL_TIME_UPTIME_RATIO + " REAL NULL" +
                " );";

        final String SQL_CREATE_MONITOR_LOG_TABLE = "CREATE TABLE " +
                LogEntry.TABLE_NAME + " (" +
                LogEntry.COLUMN_MONITOR_ID + " INTEGER NOT NULL, " +
                LogEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                LogEntry.COLUMN_DATE_TIME + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_RESPONSE_TABLE = "CREATE TABLE " +
                ResponseEntry.TABLE_NAME + " (" +
                ResponseEntry.COLUMN_MONITOR_ID + " INTEGER NOT NULL, " +
                ResponseEntry.COLUMN_VALUE + " INTEGER NOT NULL, " +
                ResponseEntry.COLUMN_DATE_TIME + " TEXT NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_ACCOUNT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MONITOR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MONITOR_LOG_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RESPONSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AccountEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MonitorEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LogEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ResponseEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
