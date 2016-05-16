package org.hackathon_ocw.androidclient;

import android.content.Intent;
import android.net.Uri;
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
import com.golshadi.majid.report.listener.DownloadManagerListener;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class DownloadListActivity extends AppCompatActivity implements DownloadManagerListener {
    private DownloadManagerPro dm;
    private ListView downloadList;
    private DownloadListAdapter downloadListAdapter;
    private Tracker mTracker;
    private final static String TAG = "DownloadListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomApplication application = (CustomApplication) getApplication();
        mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_download_list);

        downloadList = (ListView) findViewById(R.id.download_list);
        downloadListAdapter = new DownloadListAdapter(this);
        downloadList.setAdapter(downloadListAdapter);

        detailToolBarInit();

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

        this.dm = new DownloadManagerPro(this.getApplicationContext());
        dm.init("xuesha", 6, this);
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
            StringBuilder sb = new StringBuilder();
            sb.append("开始下载：\"");
            sb.append(title);
            sb.append("\"");

            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

            int taskId = dm.addTask(courseId, videoUrl, true, false);
            try {
                dm.startDownload(taskId);
            } catch (IOException e) {
                e.printStackTrace();
            }

            HashMap<String, String> item = new HashMap<>();
            item.put(Constants.KEY_ID, courseId);
            item.put(Constants.KEY_TITLE, title);
            item.put(Constants.KEY_DESCRIPTION, description);
            item.put(Constants.KEY_THUMB_URL, thumbUrl);
            item.put("taskId", String.valueOf(taskId));
            item.put("percent", "0");

            downloadListAdapter.addItem(item);
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
        downloadListAdapter.updateProgress(taskId, percent);
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
        downloadListAdapter.updateData();
    }

    @Override
    public void connectionLost(long taskId) {
        Log.d(TAG, "connectionLost");
    }
}
