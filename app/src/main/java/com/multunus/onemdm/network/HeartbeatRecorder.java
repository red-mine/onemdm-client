package com.multunus.onemdm.network;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.multunus.onemdm.config.Config;
import com.multunus.onemdm.heartbeat.HeartbeatListener;
import com.multunus.onemdm.util.Logger;

import org.json.JSONObject;

import java.util.Calendar;

public class HeartbeatRecorder {

    public void sendHeartbeatToServer(final Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new CustomJsonObjectRequest(
                Request.Method.POST,
                Config.HEARTBEAT_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            configureNextHeartbeat(context,response.getLong("next_heartbeat_time"));
                        }
                        catch (Exception ex){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        configureNextHeartbeatForRetry(context);
                        Logger.warning(error.toString());
                    }
                },
                context
        );
        requestQueue.add(request);
    }

    public void configureNextHeartbeat(Context context,long nextHeartbeatTime) {
        configureNextHeartbeatWithMilliSeconds(context,nextHeartbeatTime * 1000);
    }

    public  void configureNextHeartbeatForRetry(Context context) {
        configureNextHeartbeatWithMilliSeconds(context,getDefaultNextHeartbeatTime());
    }

    public void configureNextHeartbeatWithMilliSeconds(Context context,long nextHeartbeatTime) {
        Logger.debug(" next heartbeat time " + nextHeartbeatTime);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HeartbeatListener.class);
        intent.setAction("com.multunus.onemdm.heartbeat.HeartbeatListener");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                nextHeartbeatTime, sender);
    }
    
    private long getDefaultNextHeartbeatTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, Config.DEFAULT_HEARTBEAT_RETRY_INTERVAL);
        Logger.debug(calendar.getTime().toString());
        return calendar.getTimeInMillis();
    }

}
