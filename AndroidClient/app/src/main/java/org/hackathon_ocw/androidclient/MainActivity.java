package org.hackathon_ocw.androidclient;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;

import org.hackathon_ocw.androidclient.Download_data.download_complete;
import org.hackathon_ocw.androidclient.wxapi.WXEntryActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, download_complete {

    private final static String Url = "http://api.jieko.cc/user/";

    //Views of the page
    public View footerLayout;
    public ListView mListView;
    private TextView textMore;
    private NavigationView navigationView;

    //Toolbars
    private Toolbar toolbar;
    private ProgressBar progressBar;

    public ListAdapter mListAdapter;
    private RefreshLayout mRefreshLayout;

    private String access_token;
    private String openid;
    private boolean login = false;
    private int positionYixi;
    private Tracker mTracker;

    public ArrayList<HashMap<String, String>> courseList = new ArrayList<HashMap<String, String>>();
    public static MainActivity Self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkNetworkStatus()) {
            UserProfile.init(getApplicationContext());

            // Obtain the shared Tracker instance.
            CustomApplication application = (CustomApplication) getApplication();
            mTracker = application.getDefaultTracker();

            TextView titleMainToolBar = (TextView) findViewById(R.id.titleMainToolBar);
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

    public boolean checkNetworkStatus() {
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

    public void naviViewInit() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void drawerInit() {
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
        textMore = (TextView) footerLayout.findViewById(R.id.text_more);
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
                Toast.makeText(getApplicationContext(), "玩命加载中...", Toast.LENGTH_SHORT).show();
                mListAdapter.clear();

                Download_data download_data = new Download_data(MainActivity.this);
                download_data.download_data_from_link(Url + UserProfile.getInstance().getUserid() + "/Candidates");
                mListAdapter.addAll(courseList);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "推荐引擎有20条更新", Toast.LENGTH_SHORT).show();
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


        final Download_data download_data = new Download_data(this);
        download_data.download_data_from_link(Url + UserProfile.getInstance().getUserid() + "/Candidates");

        mListView.setItemsCanFocus(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    boolean isYixi = false;

                    if (mListAdapter.getWebUrlbyPosition(position).contains("yixi")) {
                        parseYixiCourseStep1(mListAdapter.getWebUrlbyPosition(position));
                        positionYixi = position;
                        isYixi = true;
                    }

                    if (!mListAdapter.getVideoUrlbyPosition(position).equals("") && !isYixi) {
                        //Show subpage with videoUrl
                        Intent intent = new Intent();
                        intent.putExtra("id", mListAdapter.getIdbyPosition(position));
                        intent.putExtra("title", mListAdapter.getTitlebyPosition(position));
                        intent.putExtra("videoUrl", mListAdapter.getVideoUrlbyPosition(position));
                        intent.putExtra("description", mListAdapter.getDiscriptionbyPosition(position));
                        intent.putExtra("videoImg", mListAdapter.getVideoImgbyPosition(position));
                        intent.putExtra("userid", UserProfile.getInstance().getUserid());
                        if (UserProfile.getInstance().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                        }

                        intent.setClass(MainActivity.this, DetailActivity.class);
                        startActivity(intent);
                    } else if (mListAdapter.getVideoUrlbyPosition(position).equals("") && !isYixi) {
                        //Show subpage with Webview
                        Intent intent = new Intent();
                        intent.putExtra("webUrl", mListAdapter.getWebUrlbyPosition(position));
                        intent.putExtra("id", mListAdapter.getIdbyPosition(position));
                        intent.putExtra("title", mListAdapter.getTitlebyPosition(position));
                        intent.putExtra("userid", UserProfile.getInstance().getUserid());
                        if (UserProfile.getInstance().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                        }

                        intent.setClass(MainActivity.this, WebDetailActivity.class);
                        startActivity(intent);
                    }
                    //Send post to server
                    String courseId = MainActivity.this.mListAdapter.getIdbyPosition(position);
                    Runnable networkTask = new NetworkThread(UserProfile.getInstance().getUserid(), courseId, 3);
                    new Thread(networkTask).start();
                } catch (Exception e) {
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

    public void parseYixiCourseStep1(String videoUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, videoUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("vid")) {
                            Pattern pattern = Pattern.compile("(?<=vid: \').*(?=\')");
                            Matcher matcher = pattern.matcher(response);
                            if (matcher.find()) {
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

    public void parseYixiCourseStep2(String token) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://api.yixi.tv/youku.php?id=" + token;
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONObject("files").getJSONObject("3gphd").getJSONArray("segs");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String link = jsonObject.getString("url").replace("\\", "");

                            //Show subpage with videoUrl
                            Intent intent = new Intent();
                            intent.putExtra("id", mListAdapter.getIdbyPosition(positionYixi));
                            intent.putExtra("title", mListAdapter.getTitlebyPosition(positionYixi));
                            intent.putExtra("videoUrl", link);
                            intent.putExtra("description", mListAdapter.getDiscriptionbyPosition(positionYixi));
                            intent.putExtra("videoImg", mListAdapter.getVideoImgbyPosition(positionYixi));
                            intent.putExtra("userid", UserProfile.getInstance().getUserid());
                            if (UserProfile.getInstance().getNickname() != null) {
                                intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                                intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
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
        Button searchBtn = (Button) findViewById(R.id.searchBtn);
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

        Download_data download_data = new Download_data(MainActivity.this);
        download_data.download_data_from_link(Url + UserProfile.getInstance().getUserid() + "/Candidates");
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
            for (int i = 0; i < data_array.length(); i++) {
                JSONObject obj = new JSONObject(data_array.get(i).toString());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Constants.KEY_ID, String.valueOf(obj.getInt("item_id")));
                map.put(Constants.KEY_TITLE, obj.getString("title"));
                map.put(Constants.KEY_DESCRIPTION, obj.getString("description"));
                map.put(Constants.KEY_THUMB_URL, obj.getString("piclink"));
                map.put(Constants.KEY_VIDEOURL, obj.getString("courselink"));
                map.put(Constants.KEY_WEBURL, obj.getString("link"));
                map.put(Constants.KEY_DURATION, obj.getString("duration"));
                map.put(Constants.KEY_SOURCE, obj.getString("source"));
                map.put(Constants.KEY_INSTRUCTOR, obj.getString("instructor"));
                map.put(Constants.KEY_LANGUAGE, obj.getString("language"));
                map.put(Constants.KEY_SCHOOL, obj.getString("school"));
                map.put(Constants.KEY_TAGS, obj.getString("tags"));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login && !login) {
            UserProfile.getInstance().WXLogin();
            item.setChecked(false);
            item.setCheckable(true);
        } else if (id == R.id.nav_login && login) {
            UserProfile.getInstance().WXLogout();
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

    @Override
    protected void onResume() {
        super.onResume();

        BaseResp resp = WXEntryActivity.mResp;

        if (resp != null) {
            if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
                String wechatCode = ((SendAuth.Resp) resp).code;
                String get_access_token = Utils.getCodeRequest(wechatCode);

                //Send Request here
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, get_access_token, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    access_token = (String) response.get("access_token");
                                    openid = (String) response.get("openid");

                                    if (access_token != null && openid != null) {
                                        String get_user_info_url = Utils.getUserInfo(access_token, openid);
                                        UserProfile.getInstance().WXGetUserInfo(get_user_info_url);
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

    public void updateUserProfile() {
        login = true;
        CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
        com.android.volley.toolbox.ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(imageView, R.drawable.no_image, R.drawable.no_image);
        imageLoader.get(UserProfile.getInstance().getHeadimgurl(), listener);

        TextView textView = (TextView) findViewById(R.id.userName);
        textView.setText(UserProfile.getInstance().getNickname());

        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_login);
        if (login) {
            menuItem.setTitle("注销");
        }

        UserProfile.getInstance().updateLocalAndRemote();
    }

    public void updateNaviViewWithUserProfile() {
        String nickName = UserProfile.getInstance().getNickname();
        String headImgUrl = UserProfile.getInstance().getHeadimgurl();
        if (nickName != null && !nickName.equals("")) {
            login = true;
            CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
            RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
            com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
            com.android.volley.toolbox.ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(imageView, R.drawable.no_image, R.drawable.no_image);
            if (headImgUrl != null && !headImgUrl.equals("")) {
                imageLoader.get(headImgUrl, listener);
            }

            TextView textView = (TextView) findViewById(R.id.userName);
            textView.setText(nickName);

            Menu menu = navigationView.getMenu();
            MenuItem menuItem = menu.findItem(R.id.nav_login);
            if (login) {
                menuItem.setTitle("注销");
            }
        } else {
            login = false;
            CircularImage imageView = (CircularImage) findViewById(R.id.userHeadImage);
            imageView.setImageResource(R.drawable.ic_account_circle_black_48dp);
            TextView textView = (TextView) findViewById(R.id.userName);
            textView.setText("未登录");

            if (null != navigationView) {
                Menu menu = navigationView.getMenu();
                MenuItem menuItem = menu.findItem(R.id.nav_login);
                if (!login) {
                    menuItem.setTitle("登录");
                }
            }
        }
    }
}
