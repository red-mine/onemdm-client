package com.multunus.onemdm.pkg;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.multunus.onemdm.R;
import com.multunus.onemdm.config.Config;
import com.multunus.onemdm.model.Pkg;
import com.multunus.onemdm.util.Logger;

import java.util.UUID;
public class PkgInstallerService extends IntentService {

    private Context context;
    private String otaURL = "";
    private Pkg pkg;

    public PkgInstallerService() {
        super("PkgInstallerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.debug("PkgInstallerService started");
        this.context = getApplicationContext();
        this.pkg = intent.getParcelableExtra(Config.OTA_DATA);
        this.otaURL = pkg.getOtaUrl();
        Logger.debug("OTA URL " + otaURL);
        installOrDownloadPkg(otaURL);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void installOrDownloadPkg(String otaURL) {
        Logger.debug("OTA url " + otaURL);
        if (otaURL.equals("")) {
            createActionForInstall();
        } else {
            downloadAndShowInstallNotification();
        }
    }

    private void downloadAndShowInstallNotification() {
        final DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = enqueueDownload(downloadManager);
        final BroadcastReceiver receiver = configureDownloadCompleteBroadcastReceiver(
                downloadManager, downloadId);
        IntentFilter intentFilter
                = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(receiver, intentFilter);
    }

    private long enqueueDownload(DownloadManager downloadManager) {
        Uri uri = Uri.parse(otaURL);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription("Downloading...");
        request.setTitle(getString(R.string.app_name));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.zip");
        return downloadManager.enqueue(request);
    }

    private BroadcastReceiver configureDownloadCompleteBroadcastReceiver(
            final DownloadManager downloadManager, final long downloadId) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);

                if (cursor.moveToFirst()) {
                    int columnIndex = cursor
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == cursor
                            .getInt(columnIndex)) {
                        Logger.debug(" download successfully completed");
                        context.unregisterReceiver(this);
                        showPkgInstallNotification();
                    }
                    cursor.close();
                }
            }
        };
    }

    private void showPkgInstallNotification() {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent resultPendingIntent = createActionForInstallAfterDownload();

        createNotificationForInstallAndSaveToPreferences(notificationManager, resultPendingIntent);
    }

    private void createActionForInstall(){
        final Uri marketUri = Uri.parse("market://details?id=" + pkg.getFingerPrint());
        Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                getUniqueId(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationForInstallAndSaveToPreferences(notificationManager, pendingIntent);
    }

    private PendingIntent createActionForInstallAfterDownload() {
        Intent pendingIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        Uri otaURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/update.zip"));
        pendingIntent.setData(otaURI);
        pendingIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        pendingIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, pkg.getFingerPrint());
        pendingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return PendingIntent.getActivity(
                context,
                getUniqueId(),
                pendingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void createNotificationForInstallAndSaveToPreferences(NotificationManager notificationManager,
                                                                  PendingIntent resultPendingIntent) {
        String CHANNEL_ID = "PkgInstallerService Channel ID";
        String CHANNEL_NAME = "PkgInstallerService Channel";

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder notificationBuilder = new Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(pkg.getName())
                .setSmallIcon(R.drawable.googleg_standard_color_18)
                .setContentText("Click to Install ")
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setOngoing(true);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = notificationBuilder.build();
        }
        else{
            notification = notificationBuilder.getNotification();
        }

        notificationManager.notify(getUniqueId(), notification);
        savePkgtoPreferences();
    }

    private void savePkgtoPreferences(){
        SharedPreferences.Editor editor = this.context.getSharedPreferences(
                Config.PREFERENCE_TAG, Context.MODE_PRIVATE).edit();
        editor.putLong(pkg.getFingerPrint(), pkg.getId());
        editor.apply();
    }

    protected int getUniqueId(){
        return UUID.randomUUID().hashCode();
    }
}
