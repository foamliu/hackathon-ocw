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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import me.gujun.android.taggroup.TagGroup;

/**
 * Created by foamliu on 2016/4/11.
 */
public class SearchActivity extends AppCompatActivity {

    static final String Url = "http://api.jieko.cc/items/search/";

    private Button cancelBtn;
    private Toolbar searchToolbar;
    private TagGroup tagGroup;

    public ArrayList<String> tagsList = new ArrayList<String>();

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
        tagGroup = (TagGroup)findViewById(R.id.tagGroup);
        searchTagsInit();
    }

    public void detailToolBarInit(){
        searchToolbar = (Toolbar) findViewById(R.id.searchToolbar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        searchToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);

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

    public void searchTagsInit(){
        //String url = "http://jieko.cc/user/" + UserProfile.getUserProfile().getUserid() + "/tags";
        String url = "http://jieko.cc/user/5/tags";
        //Send Request here
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("tags");
                            for (int i = 0 ; i < jsonArray.length() ; i++)
                            {
                                JSONArray jr=jsonArray.getJSONArray(i);
                                tagsList.add((String) jr.get(0));
                            }
                            tagGroup.setTags(tagsList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        });
        requestQueue.add(jsonRequest);

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
