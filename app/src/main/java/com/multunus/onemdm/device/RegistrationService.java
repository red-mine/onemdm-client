package com.multunus.onemdm.device;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.multunus.onemdm.OneMDMService;
import com.multunus.onemdm.config.Config;
import com.multunus.onemdm.network.DeviceRegistration;

public class RegistrationService extends IntentService {

    private final DeviceRegistration deviceRegistration;

    public RegistrationService() {
        super("RegistrationService");
        this.deviceRegistration = new DeviceRegistration();
    }

    RegistrationService(DeviceRegistration deviceRegistration) {
        super("RegistrationService");
        this.deviceRegistration = deviceRegistration;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(Config.PREFERENCE_TAG, "inside RegistrationService.onHandleIntent");
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(Config.PREFERENCE_TAG,"Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    final String gcmToken = task.getResult();

                    Log.d(Config.PREFERENCE_TAG,"GCM Registration Token: " + gcmToken);
                    deviceRegistration.sendRegistrationRequestToServer(getApplicationContext(), gcmToken);
                    startService(new Intent(getApplicationContext(), OneMDMService.class));
                }
            });
    }

}
