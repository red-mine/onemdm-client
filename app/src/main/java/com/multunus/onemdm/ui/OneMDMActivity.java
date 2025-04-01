package com.multunus.onemdm.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// import com.crashlytics.android.Crashlytics;
import com.multunus.onemdm.BuildConfig;
import com.multunus.onemdm.R;
import com.multunus.onemdm.device.RegistrationService;
import com.multunus.onemdm.util.Logger;

//import io.fabric.sdk.android.Fabric;

public class OneMDMActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug("inside OneMDMActivity.onCreate");
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        if(!BuildConfig.DEBUG) {
//            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_one_mdm);
        if(isNetworkAvailable()) {
            // registerDevice();
        }
        else{
            notifyFailure();
        }
    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        });
    
    private void registerDevice() {
        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    private void notifyFailure() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Make sure that you are connected to the internet and then retry")
                .setTitle("Connectivity Issue")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
