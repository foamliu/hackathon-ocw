package org.hackathon_ocw.androidclient;

import org.hackathon_ocw.androidclient.Download_data.download_complete;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hackathon_ocw.androidclient.wxapi.WXEntryActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.*;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, download_complete {

    //Views of the page
    public View footerLayout;
    public ListView mListView;
    private TextView titleMainToolBar;
    private TextView textMore;

    //Toolbars
    private Toolbar toolbar;
    private ProgressBar progressBar;

    public ListAdapter mListAdapter;
    private RefreshLayout mRefreshLayout;

    //Wechat login
    private IWXAPI WXapi;
    private String wechatCode;
    private static String get_access_token = "";
    private String access_token;
    private String openid;

    //User info
    private UserProfile userProfile;
    /*
    private String nickname;
    private int sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    */

    private Tracker mTracker;

    public ArrayList<HashMap<String, String>> courseList = new ArrayList<HashMap<String, String>>();

    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_THUMB_URL = "thumb_url";
    static final String KEY_VIDEOURL = "videoUrl";
    static final String KEY_DURATION = "videoDuration";
    static final String Url = "http://api.jieko.cc/user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getUserProfileFromFile();

        // Obtain the shared Tracker instance.
        CustomApplication application = (CustomApplication) getApplication();
        mTracker = application.getDefaultTracker();

        titleMainToolBar=(TextView)findViewById(R.id.titleMainToolBar);
        titleMainToolBar.setText("学啥");

        toolbarInit();
        listViewInit();
        //floatingButtonInit();
        drawerInit();
        naviViewInit();

    }

    public void naviViewInit(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void drawerInit(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    public void listViewInit() {
        mListView = (ListView) findViewById(R.id.list);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.swipeContainer);

        footerLayout = getLayoutInflater().inflate(R.layout.listview_footer, null);
        textMore = (TextView)footerLayout.findViewById(R.id.text_more);
        progressBar = (ProgressBar) footerLayout.findViewById(R.id.load_progress_bar);
        textMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadData();
            }
        });

        mListView.addFooterView(footerLayout);
        mRefreshLayout.setChildView(mListView);
        mListAdapter = new ListAdapter(this, courseList);
        mListView.setAdapter(mListAdapter);

        mRefreshLayout.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright);


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //fetchTimeLineAsync(0);
                Toast.makeText(getApplicationContext(), "正在刷新... ", Toast.LENGTH_SHORT).show();
                mListAdapter.clear();

                Download_data download_data = new Download_data((download_complete) MainActivity.this);
                download_data.download_data_from_link(Url + Long.toString(new GetUserIdFromIP().getUserId()) + "/Candidates");
                mListAdapter.addAll(courseList);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "刷新完成!", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 4000);
            }
        });

        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                LoadData();
            }
        });


        final Download_data download_data = new Download_data((download_complete) this);
        download_data.download_data_from_link(Url + Long.toString(new GetUserIdFromIP().getUserId()) + "/Candidates");

        mListView.setItemsCanFocus(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Show subpage
                Intent intent = new Intent();
                intent.putExtra("id", mListAdapter.getIdbyPosition(position));
                intent.putExtra("title", mListAdapter.getTitlebyPosition(position));
                intent.putExtra("videoUrl", mListAdapter.getVideoUrlbyPosition(position));
                intent.putExtra("description", mListAdapter.getDiscriptionbyPosition(position));
                intent.putExtra("videoImg", mListAdapter.getVideoImgbyPosition(position));
                intent.putExtra("userid", userProfile.getUserid());
                if(userProfile.getNickname() != null) {
                    intent.putExtra("nickname", userProfile.getNickname());
                    intent.putExtra("headimgurl", userProfile.getHeadimgurl());
                }

                intent.setClass(MainActivity.this, DetailActivity.class);
                startActivity(intent);

                //Send post to server
                String courseId = MainActivity.this.mListAdapter.getIdbyPosition(position);
                Runnable networkTask = new NetworkThread(courseId, 3);
                new Thread(networkTask).start();

                //Send event to Google Analytics
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Mainpage")
                        .setAction("Click the ocw item")
                        .setLabel(mListAdapter.getIdbyPosition(position))
                        .setValue(1)
                        .build());
            }
        });
    }

    /*
    public void floatingButtonInit() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Foam like it!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    */

    public void toolbarInit() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void LoadData() {
        // start to load
        Toast.makeText(getApplicationContext(), "加载更多", Toast.LENGTH_SHORT).show();

        textMore.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Download_data download_data = new Download_data((download_complete) MainActivity.this);
        download_data.download_data_from_link(Url + Long.toString(new GetUserIdFromIP().getUserId()) + "/Candidates");
        mListAdapter.addAll(courseList);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "刷新完成!", Toast.LENGTH_SHORT).show();
                //swipeContainer.setRefreshing(false);
                mRefreshLayout.setLoading(false);
                mListAdapter.notifyDataSetChanged();
                textMore.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 4000);
    }

    @Override
    public void get_data(String data) {
        try {
            //JSONArray data_array=new JSONArray(data);
            JSONObject object = new JSONObject(data);
            JSONArray data_array = object.getJSONArray("courses");
            for (int i = 0 ; i < data_array.length() ; i++)
            {
                JSONObject obj=new JSONObject(data_array.get(i).toString());

                HashMap<String, String>map = new HashMap<String,String>();
                map.put(KEY_ID,String.valueOf(obj.getInt("item_id")));
                map.put(KEY_TITLE,obj.getString("title"));
                map.put(KEY_DESCRIPTION,obj.getString("description"));
                map.put(KEY_THUMB_URL,obj.getString("piclink"));
                map.put(KEY_VIDEOURL,obj.getString("courselink"));
                map.put(KEY_DURATION,obj.getString("duration"));
                courseList.add(map);

            }
            mListAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            WXLogin();
        }
        /*
        else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }
        */
        else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Append Wechat login function
    public static String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    //获取用户个人信息
    public static String GetUserInfo="https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    private void WXLogin() {
        WXapi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);
        if(!WXapi.isWXAppInstalled())
        {
            Toast.makeText(getApplicationContext(), "请先安装微信应用", Toast.LENGTH_SHORT).show();
            return;
        }
        WXapi.registerApp(Constants.APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "hackathon_ocw";
        boolean res = WXapi.sendReq(req);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BaseResp resp = WXEntryActivity.mResp;

        if(resp != null)
        {
            if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
                wechatCode = ((SendAuth.Resp)resp).code;
                get_access_token = getCodeRequest(wechatCode);

                //Send Request here
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, get_access_token, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    access_token = (String) response.get("access_token");
                                    openid = (String) response.get("openid");

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String get_user_info_url = getUserInfo(access_token, openid);
                                WXGetUserInfo(get_user_info_url);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                });
                requestQueue.add(jsonRequest);
            }
        }

    }

    public void WXGetUserInfo(String url)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            userProfile.setNickname((String)response.get("nickname"));
                            userProfile.setSex((Integer) response.get("sex"));
                            userProfile.setCity((String) response.get("city"));
                            userProfile.setProvince((String) response.get("province"));
                            userProfile.setCountry((String) response.get("country"));
                            userProfile.setHeadimgurl((String) response.get("headimgurl"));
                            //Toast.makeText(getApplicationContext(), nickname + " " + country , Toast.LENGTH_SHORT).show();
                            UpdateUserProfile();
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(
                    NetworkResponse arg0) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(
                            arg0.data, "UTF-8"));
                    return Response.success(jsonObject,
                            HttpHeaderParser.parseCacheHeaders(arg0));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception je) {
                    return Response.error(new ParseError(je));
                }
            }

        };

        requestQueue.add(jsonRequest);
    }

    public static String getUserInfo(String access_token,String openid){
        String result = null;
        GetUserInfo = GetUserInfo.replace("ACCESS_TOKEN",
                urlEnodeUTF8(access_token));
        GetUserInfo = GetUserInfo.replace("OPENID",
                urlEnodeUTF8(openid));
        result = GetUserInfo;
        return result;
    }

    public static String getCodeRequest(String code) {
        String result = null;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEnodeUTF8(Constants.APP_ID));
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEnodeUTF8(Constants.APP_SECRET));
        GetCodeRequest = GetCodeRequest.replace("CODE",urlEnodeUTF8(code));
        result = GetCodeRequest;
        return result;
    }


    public static String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void UpdateUserProfile()
    {
        CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
        com.android.volley.toolbox.ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(imageView,R.drawable.no_image, R.drawable.no_image);
        imageLoader.get(userProfile.getHeadimgurl(), listener);

        TextView textView = (TextView)findViewById(R.id.userName);
        textView.setText(userProfile.getNickname());

        //Update local user profile
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("userid", userProfile.getUserid());
            jsonObject.put("nickname", userProfile.getNickname());
            jsonObject.put("sex", userProfile.getSex());
            jsonObject.put("city", userProfile.getCity());
            jsonObject.put("province", userProfile.getProvince());
            jsonObject.put("country", userProfile.getCountry());
            jsonObject.put("headimgurl", userProfile.getHeadimgurl());
            if(userProfile.getDeviceid() == null)
            {
                userProfile.setDeviceid(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            }
            jsonObject.put("deviceid", userProfile.getDeviceid());
        }catch (Exception e)
        {
            Log.e("Json Error",e.toString());
        }

        String fileName = "userProfile.json";
        File userProfileFile = new File(getApplicationContext().getFilesDir(), fileName);
        try {
            FileWriter fw = new FileWriter(userProfileFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            userProfileFile = null;
        }
    }

    public void getUserProfileFromFile()
    {
        userProfile = new UserProfile();
        String str;
        //Read from local user profile
        try {
            InputStream inputStream = openFileInput("userProfile.json");

            if (inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                str = stringBuilder.toString();

                if(!str.contains("userid"))
                {
                    getUserId();
                    return;
                }
                //Parse
                try{
                    JSONObject jsonObject = new JSONObject(str);
                    userProfile.setUserid(jsonObject.getString("userid"));
                    userProfile.setDeviceid(jsonObject.getString("deviceid"));
                    userProfile.setNickname(jsonObject.getString("nickname"));
                    userProfile.setSex(jsonObject.getInt("sex"));
                    userProfile.setCity(jsonObject.getString("city"));
                    userProfile.setProvince(jsonObject.getString("province"));
                    userProfile.setCountry(jsonObject.getString("country"));
                    userProfile.setHeadimgurl(jsonObject.getString("headimgurl"));
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e) {
            getUserId();
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }

    public void setUserProfile() {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("userid", userProfile.getUserid());
            Toast.makeText(getApplicationContext(),  userProfile.getUserid(), Toast.LENGTH_SHORT).show();
        }catch (Exception e)
        {
            Log.e("Json Error",e.toString());
        }

        String fileName = "userProfile.json";
        File userProfileFile = new File(getApplicationContext().getFilesDir(), fileName);
        try {
            FileWriter fw = new FileWriter(userProfileFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            userProfileFile = null;
        }

    }

    public void getUserId(){
        String url = "http://jieko.cc/user";
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        userProfile.setDeviceid(android_id);
        //Send POST request
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("deviceid", android_id);
        }catch (Exception e)
        {
            Log.e("Json Error",e.toString());
        }
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            userProfile.setUserid(String.valueOf((Long)response.getLong("userid")));
                            setUserProfile();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        requestQueue.add(jsonRequest);
    }
}
