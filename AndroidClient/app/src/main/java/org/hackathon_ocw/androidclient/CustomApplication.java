package org.hackathon_ocw.androidclient;

import android.app.Application;

import org.acra.*;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.*;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


/**
 * Created by dianyang on 2016/3/9.
 */
@ReportsCrashes(
        formUri = "http://api.jieko.cc/crashReport",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class CustomApplication extends Application {
    @Override
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);
    }
}
