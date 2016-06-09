package org.hackathon_ocw.androidclient.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.adapter.HistoryListAdapter;
import org.hackathon_ocw.androidclient.util.SystemBarTintManager;

public class HistoryActivity extends AppCompatActivity {

    private HistoryListAdapter historyListAdapter;
    private final static String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        detailToolBarInit();

        ListView historyList = (ListView) findViewById(R.id.history_list);
        historyListAdapter = new HistoryListAdapter(this);
        historyList.setAdapter(historyListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                historyListAdapter.notifyDataSetChanged();
            }
        } , 1000);
    }

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
        titleToolBar.setText("历史记录");

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

}
