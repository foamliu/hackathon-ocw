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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hackathon_ocw.androidclient.wxapi.WXEntryActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
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
    private NavigationView navigationView;

    //Toolbars
    private Toolbar toolbar;
    private Button searchBtn;
    private ProgressBar progressBar;

    public ListAdapter mListAdapter;
    private RefreshLayout mRefreshLayout;

    //Wechat login
    private IWXAPI WXapi;
    private String wechatCode;
    private static String get_access_token = "";
    private String access_token;
    private String openid;
    private boolean login = false;
    private int positionYixi;
    private Tracker mTracker;

    public ArrayList<HashMap<String, String>> courseList = new ArrayList<HashMap<String, String>>();
    public static MainActivity Self;

    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_THUMB_URL = "thumb_url";
    static final String KEY_VIDEOURL = "videoUrl";
    static final String KEY_WEBURL = "webUrl";
    static final String KEY_DURATION = "videoDuration";
    static final String KEY_SOURCE = "source";
    static final String KEY_INSTRUCTOR = "instructor";
    static final String KEY_LANGUAGE = "language";
    static final String KEY_SCHOOL = "school";
    static final String KEY_TAGS = "tags";
    static final String Url = "http://api.jieko.cc/user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkNetworkStatus())
        {
            getUserProfileFromFile();

            // Obtain the shared Tracker instance.
            CustomApplication application = (CustomApplication) getApplication();
            mTracker = application.getDefaultTracker();

            titleMainToolBar=(TextView)findViewById(R.id.titleMainToolBar);
            titleMainToolBar.setText("学啥");

            toolbarInit();
            searchBtnInit();
            listViewInit();
            //floatingButtonInit();
            drawerInit();
            naviViewInit();
        }

        MainActivity.Self = this;
    }

    public boolean checkNetworkStatus(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("网络提示信息");
                builder.setMessage("网络不可用，如果继续，请先设置网络！");
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        if (android.os.Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName component = new ComponentName(
                                    "com.android.settings",
                                    "com.android.settings.WirelessSettings");
                            intent.setComponent(component);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        startActivity(intent);
                        finish();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                return false;
            }
        return true;
    }

    public void naviViewInit(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
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
                download_data.download_data_from_link(Url + UserProfile.getUserProfile().getUserid() + "/Candidates");
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
        download_data.download_data_from_link(Url + UserProfile.getUserProfile().getUserid() + "/Candidates");

        mListView.setItemsCanFocus(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    boolean isYixi = false;

                    if(mListAdapter.getWebUrlbyPosition(position).contains("yixi")){
                        parseYixiCourseStep1(mListAdapter.getWebUrlbyPosition(position));
                        positionYixi = position;
                        isYixi = true;
                    }

                    if(!mListAdapter.getVideoUrlbyPosition(position).equals("") && !isYixi){
                        //Show subpage with videoUrl
                        Intent intent = new Intent();
                        intent.putExtra("id", mListAdapter.getIdbyPosition(position));
                        intent.putExtra("title", mListAdapter.getTitlebyPosition(position));
                        intent.putExtra("videoUrl", mListAdapter.getVideoUrlbyPosition(position));
                        intent.putExtra("description", mListAdapter.getDiscriptionbyPosition(position));
                        intent.putExtra("videoImg", mListAdapter.getVideoImgbyPosition(position));
                        intent.putExtra("userid", UserProfile.getUserProfile().getUserid());
                        if(UserProfile.getUserProfile().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getUserProfile().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getUserProfile().getHeadimgurl());
                        }

                        intent.setClass(MainActivity.this, DetailActivity.class);
                        startActivity(intent);
                    }
                    else if (mListAdapter.getVideoUrlbyPosition(position).equals("") && !isYixi){
                        //Show subpage with Webview
                        Intent intent = new Intent();
                        intent.putExtra("webUrl",mListAdapter.getWebUrlbyPosition(position));
                        intent.putExtra("id", mListAdapter.getIdbyPosition(position));
                        intent.putExtra("title", mListAdapter.getTitlebyPosition(position));
                        intent.putExtra("userid", UserProfile.getUserProfile().getUserid());
                        if(UserProfile.getUserProfile().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getUserProfile().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getUserProfile().getHeadimgurl());
                        }

                        intent.setClass(MainActivity.this, WebDetailActivity.class);
                        startActivity(intent);
                    }
                    //Send post to server
                    String courseId = MainActivity.this.mListAdapter.getIdbyPosition(position);
                    Runnable networkTask = new NetworkThread(UserProfile.getUserProfile().getUserid(), courseId, 3);
                    new Thread(networkTask).start();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

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

    public void parseYixiCourseStep1(String videoUrl){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = videoUrl;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.contains("vid")){
                            Pattern pattern = Pattern.compile("(?<=vid: \').*(?=\')");
                            Matcher matcher = pattern.matcher(response);
                            if(matcher.find()) {
                                parseYixiCourseStep2(matcher.group(0));
                                //Log.e("Get regex", matcher.group(0));
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Get error", error.toString());
            }
        });
        queue.add(stringRequest);
    }

    public void parseYixiCourseStep2(String token){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://api.yixi.tv/youku.php?id=" + token;
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONObject("files").getJSONObject("3gphd").getJSONArray("segs");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String link = jsonObject.getString("url").replace("\\","");

                            //Show subpage with videoUrl
                            Intent intent = new Intent();
                            intent.putExtra("id", mListAdapter.getIdbyPosition(positionYixi));
                            intent.putExtra("title", mListAdapter.getTitlebyPosition(positionYixi));
                            intent.putExtra("videoUrl", link);
                            intent.putExtra("description", mListAdapter.getDiscriptionbyPosition(positionYixi));
                            intent.putExtra("videoImg", mListAdapter.getVideoImgbyPosition(positionYixi));
                            intent.putExtra("userid", UserProfile.getUserProfile().getUserid());
                            if(UserProfile.getUserProfile().getNickname() != null) {
                                intent.putExtra("nickname", UserProfile.getUserProfile().getNickname());
                                intent.putExtra("headimgurl", UserProfile.getUserProfile().getHeadimgurl());
                            }

                            intent.setClass(MainActivity.this, DetailActivity.class);
                            startActivity(intent);
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

    public void toolbarInit() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);
    }

    public void searchBtnInit() {
        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onSearchRequested();
                //TODO:create a new view
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
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
        download_data.download_data_from_link(Url + UserProfile.getUserProfile().getUserid() + "/Candidates");
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
            courseList.clear();
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
                map.put(KEY_WEBURL,obj.getString("link"));
                map.put(KEY_DURATION,obj.getString("duration"));
                map.put(KEY_SOURCE,obj.getString("source"));
                map.put(KEY_INSTRUCTOR,obj.getString("instructor"));
                map.put(KEY_LANGUAGE,obj.getString("language"));
                map.put(KEY_SCHOOL,obj.getString("school"));
                map.put(KEY_TAGS,obj.getString("tags"));
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        updateNaviViewWithUserProfile();
        return true;
    }

    /*
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

        if (id == R.id.nav_login && login == false) {
            WXLogin();
            item.setChecked(false);
            item.setCheckable(true);
        }
        else if(id == R.id.nav_login && login == true){
            WXLogout();
            item.setChecked(false);
            item.setCheckable(true);
        }
        /*
        else if (id == R.id.nav_gallery) {
        }
        else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }
        else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        */

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

    private void WXLogout(){
        //clear userprofile.json
        UserProfile.getUserProfile().clearProfile();

        updateNaviViewWithUserProfile();

        //Update local user profile
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("userid", UserProfile.getUserProfile().getUserid());
            if(UserProfile.getUserProfile().getDeviceid() == null)
            {
                UserProfile.getUserProfile().setDeviceid(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            }
            jsonObject.put("deviceid", UserProfile.getUserProfile().getDeviceid());
        }catch (Exception e)
        {
            Log.e("Json Error",e.toString());
        }

        //Write to local file
        String fileName = "userProfile.json";
        File userProfileFile = new File(getApplicationContext().getFilesDir(), fileName);
        try {
            FileWriter fw = new FileWriter(userProfileFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                                    if (access_token != null && openid != null)
                                    {
                                        String get_user_info_url = getUserInfo(access_token, openid);
                                        WXGetUserInfo(get_user_info_url);
                                    }

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
                            UserProfile.getUserProfile().setOpenid((String) response.get("openid"));
                            UserProfile.getUserProfile().setNickname((String) response.get("nickname"));
                            UserProfile.getUserProfile().setSex((Integer) response.get("sex"));
                            UserProfile.getUserProfile().setCity((String) response.get("city"));
                            UserProfile.getUserProfile().setProvince((String) response.get("province"));
                            UserProfile.getUserProfile().setCountry((String) response.get("country"));
                            UserProfile.getUserProfile().setHeadimgurl((String) response.get("headimgurl"));
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
        login = true;
        CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
        com.android.volley.toolbox.ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(imageView,R.drawable.no_image, R.drawable.no_image);
        imageLoader.get(UserProfile.getUserProfile().getHeadimgurl(), listener);

        TextView textView = (TextView)findViewById(R.id.userName);
        textView.setText(UserProfile.getUserProfile().getNickname());

        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_login);
        if(login == true){
            menuItem.setTitle("注销");
        }

        //Update local user profile
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("userid", UserProfile.getUserProfile().getUserid());
            jsonObject.put("openid", UserProfile.getUserProfile().getOpenid());
            jsonObject.put("nickname", UserProfile.getUserProfile().getNickname());
            jsonObject.put("sex", UserProfile.getUserProfile().getSex());
            jsonObject.put("city", UserProfile.getUserProfile().getCity());
            jsonObject.put("province", UserProfile.getUserProfile().getProvince());
            jsonObject.put("country", UserProfile.getUserProfile().getCountry());
            jsonObject.put("headimgurl", UserProfile.getUserProfile().getHeadimgurl());
            if(UserProfile.getUserProfile().getDeviceid() == null)
            {
                UserProfile.getUserProfile().setDeviceid(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            }
            jsonObject.put("deviceid", UserProfile.getUserProfile().getDeviceid());
        }catch (Exception e)
        {
            Log.e("Json Error",e.toString());
        }


        //Write to local file
        String fileName = "userProfile.json";
        File userProfileFile = new File(getApplicationContext().getFilesDir(), fileName);
        try {
            FileWriter fw = new FileWriter(userProfileFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Send PATCH to server
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        jsonObject.remove("userid");
        jsonObject.remove("sex");
        try{
            jsonObject.put("_id", Integer.valueOf(UserProfile.getUserProfile().getUserid()));
            jsonObject.put("sex", Integer.valueOf(UserProfile.getUserProfile().getSex()));
        }catch(Exception e){
            e.printStackTrace();
        }

        String httpurl = "http://jieko.cc/user/" + UserProfile.getUserProfile().getUserid();

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, httpurl, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());

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

    public void getUserProfileFromFile()
    {
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
                    UserProfile.getUserProfile().setUserid(jsonObject.getString("userid"));
                    UserProfile.getUserProfile().setDeviceid(jsonObject.getString("deviceid"));
                    UserProfile.getUserProfile().setOpenid(jsonObject.getString("openid"));
                    UserProfile.getUserProfile().setNickname(jsonObject.getString("nickname"));
                    UserProfile.getUserProfile().setSex(jsonObject.getInt("sex"));
                    UserProfile.getUserProfile().setCity(jsonObject.getString("city"));
                    UserProfile.getUserProfile().setProvince(jsonObject.getString("province"));
                    UserProfile.getUserProfile().setCountry(jsonObject.getString("country"));
                    UserProfile.getUserProfile().setHeadimgurl(jsonObject.getString("headimgurl"));
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

    public void updateNaviViewWithUserProfile(){
        if(UserProfile.getUserProfile().getNickname() != "" && UserProfile.getUserProfile().getNickname() != null){
            login = true;
            CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
            RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
            com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
            com.android.volley.toolbox.ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(imageView,R.drawable.no_image, R.drawable.no_image);
            if(UserProfile.getUserProfile().getHeadimgurl() != ""){
                imageLoader.get(UserProfile.getUserProfile().getHeadimgurl(), listener);
            }

            TextView textView = (TextView)findViewById(R.id.userName);
            textView.setText(UserProfile.getUserProfile().getNickname());

            Menu menu = navigationView.getMenu();
            MenuItem menuItem = menu.findItem(R.id.nav_login);
            if(login == true){
                menuItem.setTitle("注销");
            }
        }
        else {
            login = false;
            CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
            imageView.setImageResource(R.drawable.ic_account_circle_black_48dp);
            TextView textView = (TextView)findViewById(R.id.userName);
            textView.setText("未登陆");

            Menu menu = navigationView.getMenu();
            MenuItem menuItem = menu.findItem(R.id.nav_login);
            if(login == false){
                menuItem.setTitle("登陆");
            }
        }
    }

    public void setUserProfile() {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("userid", UserProfile.getUserProfile().getUserid());
            //Toast.makeText(getApplicationContext(),  userProfile.getUserid(), Toast.LENGTH_SHORT).show();
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
        UserProfile.getUserProfile().setDeviceid(android_id);
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
                            UserProfile.getUserProfile().setUserid(String.valueOf((Long) response.getLong("userid")));
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
