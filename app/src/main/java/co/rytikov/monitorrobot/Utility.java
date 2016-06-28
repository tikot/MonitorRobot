package co.rytikov.monitorrobot;

import android.content.Context;
import android.support.v4.content.ContextCompat;

public class Utility {

    public static String getPercent(Context context, String p) {
        String template = context.getResources().getString(R.string.percent);
        if (p == null) p = "0";
        return String.format(template, p);
    }

    /**
     * 0 - paused, 1 - not checked yet, 2 - up, 8 - seems down, 9 - down
     * @param context Context
     * @param status int
     * @return int
     */
    public static int getStatusColor(Context context, int status) {
        switch (status) {
            case 0:
            case 1:
                return ContextCompat.getColor(context, R.color.paused);
            case 2:
                return ContextCompat.getColor(context, R.color.up);
            case 8:
            case 9:
                return ContextCompat.getColor(context, R.color.down);
            default:
                return ContextCompat.getColor(context, R.color.paused);
        }
    }

    /**
     *
     * @param name String
     * @return String
     */
    public static String getTypeID(String name) {
        switch (name) {
            default:
            case "HTTP(s)":
                return "1";
            case "Ping":
                return "3";
            case "Port":
                return "4";
        }
    }

    /**
     *
     * @param port String
     * @return String
     */
    public static String getSubTypeID(String port) {
        switch (port) {
            case "80":
                return "1";
            case "443":
                return "2";
            case "21":
                return "3";
            case "25":
                return "4";
            case "110":
                return "5";
            case "143":
                return "6";
            default:
                return "99";
        }
    }

}
