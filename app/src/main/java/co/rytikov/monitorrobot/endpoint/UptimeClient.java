package co.rytikov.monitorrobot.endpoint;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class UptimeClient {

    public static final String API_URL = "https://api.uptimerobot.com/";
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    //TODO refactor endpoints models
    public static class Account {
        public int monitorLimit;
        public int monitorInterval;
        public int upMonitors;
        public int downMonitors;
        public int pausedMonitors;
    }
    public class AccountDetails {
        public String stat;
        public Account account;
        public String getStat() {
            return stat;
        }
    }

    public class Monitor {
        public int id;
        public String friendlyname;
        public String url;
        public int type;
        public String subtype;
        public int keywordtype;
        public String keywordvalue;
        public String httpusername;
        public String httpPassword;
        public int port;
        public int interval;
        public int status;
        public double alltimeuptimeratio;
        public double customuptimeratio;
    }
    public class Monitors {
        public String stat;
        public int offset;
        public int limit;
        public int total;
        public List<Monitor> monitors;
    }

    public class MonitorId {
        public int id;
    }
    public class CallResult {
        public String stat;
        public MonitorId monitor;
    }

    /**
     * Endpoint interface for Uptime Robot API
     * @link https://uptimerobot.com/api
     */
    public interface UptimeRobot {

        /**
         * Endpoint for get account details
         *
         * https://api.uptimerobot.com/getAccountDetails?format=json&noJsonCallback=1&apiKey=
         * SUCCESS
         * {"stat": "ok",
         *  "account":{"monitorLimit":"50", "monitorInterval":"5", "upMonitors":"1",
         *             "downMonitors":"0", "pausedMonitors":"0"}
         * }
         * ERROR
         * {"stat": "fail", "id": "101", "message": "apiKey is wrong"}
         *
         * @param apiKey String
         * @return AccountDetails
         */
        @GET("getAccountDetails?format=json&noJsonCallback=1")
        Call<AccountDetails> getAccountDetails(@Query("apiKey") String apiKey);

        /**
         * Get all monitors method with full list of parameters
         * @param apiKey String - required
         * @param monitors String
         * @param types String
         * @param statuses String
         * @param customUptimeRatio String
         * @param logs String
         * @param logsLimit String
         * @param responseTimes String
         * @param responseTimesLimit String
         * @param responseTimesAverage String
         * @param responseTimesStartDate String
         * @param responseTimesEndDate String
         * @param alertContacts String
         * @param showMonitorAlertContacts String
         * @param offset String
         * @param limit String
         * @param search String
         * @return Monitors
         */
        //TODO break down to simpler endpoint calls
        @GET("getMonitors?format=json&noJsonCallback=1")
        Call<Monitors> getMonitors(
                @Query("apiKey") String apiKey,
                @Query("monitors") String monitors,
                @Query("types") String types,
                @Query("statuses") String statuses,
                @Query("customUptimeRatio") String customUptimeRatio,
                @Query("logs") String logs,
                @Query("logsLimit") String logsLimit,
                @Query("responseTimes") String responseTimes,
                @Query("responseTimesLimit") String responseTimesLimit,
                @Query("responseTimesAverage") String responseTimesAverage,
                @Query("responseTimesStartDate") String responseTimesStartDate,
                @Query("responseTimesEndDate") String responseTimesEndDate,
                @Query("alertContacts") String alertContacts,
                @Query("showMonitorAlertContacts") String showMonitorAlertContacts,
                @Query("offset") String offset,
                @Query("limit") String limit,
                @Query("search") String search


        );

        /**
         * Create a New Monitor with any type. For optional values just pass null as the value.
         *
         * @param apiKey String - required
         * @param monitorFriendlyName String - required
         * @param monitorURL String - required
         * @param monitorType String - required
         * @param monitorSubType String - optional
         * @param monitorPort String - optional
         * @param monitorInterval String - optional (in minutes)
         * @return CallResult
         */
        @GET("newMonitor?format=json&noJsonCallback=1")
        Call<CallResult> newMonitor(
                @Query("apiKey") String apiKey,
                @Query("monitorFriendlyName") String monitorFriendlyName,
                @Query("monitorURL") String monitorURL,
                @Query("monitorType") String monitorType,
                @Query("monitorSubType") String monitorSubType,
                @Query("monitorPort") String monitorPort,
                @Query("monitorInterval") String monitorInterval

        );

        /**
         * Edit Monitor
         * @param apiKey String - required
         * @param monitorFriendlyName String - required
         * @param monitorURL String - required
         * @param monitorType String - required
         * @param monitorSubType String - optional
         * @param monitorPort String - optional
         * @param monitorInterval String - optional (in minutes)
         * @return CallResult
         */
        @GET("editMonitor?format=json&noJsonCallback=1")
        Call<CallResult> editMonitor(
                @Query("apiKey") String apiKey,
                @Query("monitorID") int monitorID,
                @Query("monitorStatus") String monitorStatus,
                @Query("monitorFriendlyName") String monitorFriendlyName,
                @Query("monitorURL") String monitorURL,
                @Query("monitorType") String monitorType,
                @Query("monitorSubType") String monitorSubType,
                @Query("monitorPort") String monitorPort,
                @Query("monitorInterval") String monitorInterval
        );

        /**
         * Delete monitors with this endpoint
         * @param apiKey String - required
         * @param monitorID String - required
         * @return CallResult
         */
        @GET("deleteMonitor?format=json&noJsonCallback=1")
        Call<CallResult> deleteMonitor(
                @Query("apiKey") String apiKey,
                @Query("monitorID") String monitorID
        );

        /**
         * Reset will delete all stats and response time data in a Monitor
         * @param apiKey String - required
         * @param monitorID String - required
         * @return CallResult
         */
        @GET("resetMonitor?format=json&noJsonCallback=1")
        Call<CallResult> resetMonitor(
                @Query("apiKey") String apiKey,
                @Query("monitorID ") String monitorID
        );
    }
}
