package org.hackathon_ocw.androidclient;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.net.NetworkInfo.State;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.GoogleAnalytics;

import org.acra.*;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.*;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender;

import java.util.UUID;

/**
 * Created by dianyang on 2016/3/9.
 */
@ReportsCrashes(
        formUri = "http://api.jieko.cc/crashes",
        mode = ReportingInteractionMode.TOAST,
        customReportContent = { ReportField.USER_CRASH_DATE,ReportField.ANDROID_VERSION, ReportField.APP_VERSION_NAME, ReportField.PHONE_MODEL, ReportField.STACK_TRACE },
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        resToastText = R.string.crash_toast_text)
public class CustomApplication extends Application {

    public String uniqueId;
    public ConnectivityManager manager;

    private Tracker mTracker;

    @Override
    public void onCreate()
    {
        super.onCreate();

        if(checkNetworkState())
        {
            //only initial acra with wift
            ACRA.init(this);
        }
        //Remove all report without wifi
        //ACRA.getErrorReporter().removeAllReportSenders();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }



    public boolean checkNetworkState() {
        boolean flag = false;
        manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        if (!flag) {
            setNetwork();
        } else {
            flag = isNetworkAvailable();
        }
        return flag;
    }


    public void setNetwork(){
        Toast.makeText(this, "wifi is closed!", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("网络提示信息");
        builder.setMessage("网络不可用，如果继续，请先设置网络！");
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT > 10) {
                    intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                } else {
                    intent = new Intent();
                    ComponentName component = new ComponentName(
                            "com.android.settings",
                            "com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                }
                startActivity(intent);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        builder.show();
    }

    public boolean isNetworkAvailable(){

        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(wifi == State.CONNECTED || wifi == State.CONNECTING){
            //Toast.makeText(this, "wifi is open! wifi", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void getUUID()
    {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, tmPhone, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        uniqueId = deviceUuid.toString();
    }

}


