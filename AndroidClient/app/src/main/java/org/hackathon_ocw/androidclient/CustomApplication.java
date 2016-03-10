package org.hackathon_ocw.androidclient;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;
import android.net.NetworkInfo.State;

import org.acra.*;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.*;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;


/**
 * Created by dianyang on 2016/3/9.
 */
@ReportsCrashes(
        formUri = "http://api.jieko.cc/crashes",
        mode = ReportingInteractionMode.TOAST,
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        resToastText = R.string.crash_toast_text)
public class CustomApplication extends Application {

    public ConnectivityManager manager;

    @Override
    public void onCreate()
    {
        super.onCreate();

        if(checkNetworkState() == true)
        {
            //initial acra
            ACRA.init(this);
        }
        else
        {
            //Remove all report without wifi
            ACRA.getErrorReporter().removeAllReportSenders();
        }
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
            Toast.makeText(this, "wifi is open! wifi", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            return false;
        }
    }
}


