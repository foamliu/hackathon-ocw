package org.hackathon_ocw.androidclient;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.golshadi.majid.core.DownloadManagerPro;
import com.golshadi.majid.core.enums.TaskStates;
import com.golshadi.majid.report.listener.DownloadManagerListener;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadListActivity extends AppCompatActivity implements DownloadManagerListener {
    private DownloadManagerPro downloadManager;
    private DownloadListAdapter downloadListAdapter;
    private final static String TAG = "DownloadListActivity";
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomApplication application = (CustomApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_download_list);

        detailToolBarInit();
        downloadManagerInit();

        ListView downloadList = (ListView) findViewById(R.id.download_list);
        downloadListAdapter = new DownloadListAdapter(this, downloadManager);
        downloadList.setAdapter(downloadListAdapter);

        setTimerForDownloadProgress();
    }

    void setTimerForDownloadProgress() {
        timer = new Timer();
        TimerTask updateProfile = new CustomTimerTask();
        timer.scheduleAtFixedRate(updateProfile, 1000, 1000);
    }

    public class CustomTimerTask extends TimerTask {
        private Handler mHandler = new Handler();

        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadListAdapter.updateProgress();
                            downloadListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }).start();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String title = intent.getStringExtra(Constants.KEY_TITLE);
        String description = intent.getStringExtra(Constants.KEY_DESCRIPTION);
        String courseId = intent.getStringExtra(Constants.KEY_ID);
        String videoUrl = intent.getStringExtra(Constants.KEY_VIDEOURL);
        String thumbUrl = intent.getStringExtra(Constants.KEY_THUMB_URL);

        if (videoUrl != null && !videoUrl.equals("")) {

            HashMap<String, String> item = new HashMap<>();
            item.put(Constants.KEY_ID, courseId);
            item.put(Constants.KEY_TITLE, title);
            item.put(Constants.KEY_DESCRIPTION, description);
            item.put(Constants.KEY_THUMB_URL, thumbUrl);

            if (!downloadListAdapter.contains(item)) {
                String sb = "开始下载：\"" + title + "\"";

                Toast.makeText(this, sb, Toast.LENGTH_LONG).show();

                String saveName = courseId;
                int taskId = downloadManager.addTask(saveName, videoUrl, true, false);
                try {
                    downloadManager.startDownload(taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                item.put("taskId", String.valueOf(taskId));
                item.put("percent", "0");
                item.put("state", String.valueOf(TaskStates.INIT));
                item.put("fileSize", "0");

                downloadListAdapter.addItem(item);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressWarnings("ConstantConditions")
    private void detailToolBarInit() {
        Toolbar detailToolbar = (Toolbar) findViewById(R.id.detailToolbar);
        setSupportActionBar(detailToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        detailToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView titleToolBar = (TextView) findViewById(R.id.titleToolBar);
        titleToolBar.setText("下载");

        Button shareBtn = (Button) findViewById(R.id.shareBtn);
        shareBtn.setVisibility(View.GONE);
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void downloadManagerInit() {

        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(this, "未发现SD卡", Toast.LENGTH_LONG).show();
            return;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(this, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            StorageUtils.mkdir();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        StorageUtils.verifyStoragePermissions(this);

        this.downloadManager = new DownloadManagerPro(this.getApplicationContext());
        downloadManager.init("xuesha", 6, this);
    }

    @Override
    public void OnDownloadStarted(long taskId) {
        Log.d(TAG, "OnDownloadStarted");
    }

    @Override
    public void OnDownloadPaused(long taskId) {
        Log.d(TAG, "OnDownloadPaused");
    }

    @Override
    public void onDownloadProcess(long taskId, double percent, long downloadedLength) {
        Log.d(TAG, "onDownloadProcess " + percent);
    }

    @Override
    public void OnDownloadFinished(long taskId) {
        Log.d(TAG, "OnDownloadStarted");
    }

    @Override
    public void OnDownloadRebuildStart(long taskId) {
        Log.d(TAG, "OnDownloadRebuildStart");
    }

    @Override
    public void OnDownloadRebuildFinished(long taskId) {
        Log.d(TAG, "OnDownloadRebuildFinished");
    }

    @Override
    public void OnDownloadCompleted(long taskId) {
        Log.d(TAG, "OnDownloadCompleted");
    }

    @Override
    public void connectionLost(long taskId) {
        Log.d(TAG, "connectionLost");
    }
}
