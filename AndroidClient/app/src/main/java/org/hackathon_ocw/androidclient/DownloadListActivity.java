package org.hackathon_ocw.androidclient;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.io.IOException;

public class DownloadListActivity extends AppCompatActivity implements DownloadManagerListener {
    private DownloadManagerPro dm;
    private ListView downloadList;
    private DownloadListAdapter downloadListAdapter;
    private Tracker mTracker;

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
        addListenerOnBackButton();

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

        this.dm = new DownloadManagerPro(this.getApplicationContext());
        dm.init("Download", 1, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String courseId = intent.getStringExtra("id");
        String url = intent.getStringExtra("videoUrl");

        if (url != null && !url.equals(""))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(title);
            sb.append(courseId);
            sb.append(url);
            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

            int taskToken = dm.addTask("save_name", url, false, false);
            try {
                dm.startDownload(taskToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void detailToolBarInit(){
        Toolbar detailToolbar = (Toolbar) findViewById(R.id.detailToolbar);
        setSupportActionBar(detailToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        detailToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);

        TextView titleToolBar = (TextView) findViewById(R.id.titleToolBar);
        titleToolBar.setText("学啥");

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

    private void addListenerOnBackButton() {
        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void OnDownloadStarted(long taskId) {

    }

    @Override
    public void OnDownloadPaused(long taskId) {

    }

    @Override
    public void onDownloadProcess(long taskId, double percent, long downloadedLength) {

    }

    @Override
    public void OnDownloadFinished(long taskId) {

    }

    @Override
    public void OnDownloadRebuildStart(long taskId) {

    }

    @Override
    public void OnDownloadRebuildFinished(long taskId) {

    }

    @Override
    public void OnDownloadCompleted(long taskId) {

    }

    @Override
    public void connectionLost(long taskId) {

    }
}
