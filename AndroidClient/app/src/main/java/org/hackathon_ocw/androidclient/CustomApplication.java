package org.hackathon_ocw.androidclient;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

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
    private final static String TAG = "CustomApplication";
    //private UserProfile userProfile;

    @Override
    public void onCreate()
    {
        super.onCreate();

        if(checkNetworkState() != 0)
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

    public int checkNetworkState() {
        int networkStatus = 0;
        boolean flag = false;
        manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        if (!flag) {
            //No network
            networkStatus = 0;
        } else {
            //Wifi or 4G
            networkStatus = isNetworkAvailable();
        }
        return networkStatus;
    }

    public int isNetworkAvailable(){

        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(wifi == State.CONNECTED || wifi == State.CONNECTING){
            //Wifi connected
            return 2;
        }
        else
        {
            //4G connected
            return 1;
        }
    }
}


