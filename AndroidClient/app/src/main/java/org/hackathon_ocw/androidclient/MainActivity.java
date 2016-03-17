package org.hackathon_ocw.androidclient;

import org.hackathon_ocw.androidclient.Download_data.download_complete;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
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

    private IWXAPI api;
    private Tracker mTracker;

    public ArrayList<HashMap<String, String>> courseList = new ArrayList<HashMap<String, String>>();

    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_THUMB_URL = "thumb_url";
    static final String KEY_VIDEOURL = "videoUrl";
    static final String Url = "http://api.jieko.cc/user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Wechat call
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

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
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo_test";
            api.sendReq(req);
            finish();

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
