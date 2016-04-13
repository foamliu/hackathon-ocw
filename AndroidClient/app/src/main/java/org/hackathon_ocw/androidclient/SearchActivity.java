package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by foamliu on 2016/4/11.
 */
public class SearchActivity extends Activity {

    static final String Url = "http://api.jieko.cc/items/search/";

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
    }
    
}
