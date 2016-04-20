package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by foamliu on 2016/4/11.
 */
public class SearchActivity extends AppCompatActivity {

    static final String Url = "http://api.jieko.cc/items/search/";

    private Button cancelBtn;
    private Toolbar searchToolbar;
    private TextView titleToolBar;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        /*
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i("search", "query=" + query);

            final Download_data download_data = new Download_data((Download_data.download_complete) MainActivity.Self);
            try{
                String strUTF8 = URLEncoder.encode(query, "UTF-8");
                download_data.download_data_from_link(Url + strUTF8);
                finish();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        */

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_search);

        detailToolBarInit();

        addListenerOnBackButton();
    }

    public void detailToolBarInit(){
        searchToolbar = (Toolbar) findViewById(R.id.searchToolbar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        searchToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);

        //titleToolBar=(TextView)findViewById(R.id.titleToolBar);
        //titleToolBar.setText("学啥");
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

    public void addListenerOnBackButton() {
        cancelBtn = (Button)findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    
}
