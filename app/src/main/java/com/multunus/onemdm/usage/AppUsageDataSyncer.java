package com.multunus.onemdm.usage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.multunus.onemdm.network.AppUsageRecorder;
import com.multunus.onemdm.util.Logger;

public class AppUsageDataSyncer extends BroadcastReceiver {
    private final AppUsageRecorder appUsagerecorder;

    public AppUsageDataSyncer() {
        this.appUsagerecorder = new AppUsageRecorder();
    }

    AppUsageDataSyncer(AppUsageRecorder appUsagerecorder) {
        this.appUsagerecorder = appUsagerecorder;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.debug("AppUsageDataSyncer broadcast received for alarm manager");

        appUsagerecorder.sendAppUsageDataToServer(context);
    }

}
