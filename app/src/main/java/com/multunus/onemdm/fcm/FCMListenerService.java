package com.multunus.onemdm.fcm;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.multunus.onemdm.config.Config;
import com.multunus.onemdm.device.RegistrationService;
import com.multunus.onemdm.model.App;
import com.multunus.onemdm.model.Pkg;
import com.multunus.onemdm.app.AppInstallerService;
import com.multunus.onemdm.pkg.PkgInstallerService;
import com.multunus.onemdm.util.Logger;

public class FCMListenerService extends FirebaseMessagingService {

    // [START refresh_token]
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);
    }

    // [START receive_message]
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Logger.debug("data " + remoteMessage.getData().toString());
        String message = remoteMessage.getData().get("message");
        String type = remoteMessage.getData().get("type");
        Logger.debug("Message: " + message);
        Logger.debug("Type: " + type);

        if (type.equals("app")) {
            Intent intent = new Intent(this, AppInstallerService.class);
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            App app = gson.fromJson(message, App.class);
            Logger.debug(" app ID" + app.getId());
            Logger.debug(" app package name " + app.getPackageName());
            Logger.debug(" APK URL = " + app.getApkUrl());
            intent.putExtra(Config.APP_DATA, app);
            startService(intent);
        } else {
            Intent intent = new Intent(this, PkgInstallerService.class);
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            Pkg pkg = gson.fromJson(message, Pkg.class);
            Logger.debug(" pkg ID" + pkg.getId());
            Logger.debug(" pkg finger print " + pkg.getFingerPrint());
            Logger.debug(" OTA URL = " + pkg.getOtaUrl());
            intent.putExtra(Config.OTA_DATA, pkg);
            startService(intent);
        }
    }

}