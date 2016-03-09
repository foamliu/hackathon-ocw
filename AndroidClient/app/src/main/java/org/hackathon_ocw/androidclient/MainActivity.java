package org.hackathon_ocw.androidclient;

import org.hackathon_ocw.androidclient.Download_data.download_complete;
import org.hackathon_ocw.androidclient.NetworkThread;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, download_complete {

    public ListView mListView;
    public View footerLayout;
    public ArrayList<HashMap<String, String>> courseList = new ArrayList<HashMap<String, String>>();
    public ListAdapter mAdapter;
    private RefreshLayout mRefreshLayout;
    private TextView textMore;
    private ProgressBar progressBar;
    private IWXAPI api;

    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_THUMB_URL = "thumb_url";
    static final String KEY_URL = "url";
    static final String Url = "http://api.jieko.cc/user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

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
        mAdapter = new ListAdapter(this, courseList);
        mListView.setAdapter(mAdapter);

        mRefreshLayout.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright);


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //fetchTimeLineAsync(0);
                Toast.makeText(getApplicationContext(), "正在刷新... ", Toast.LENGTH_SHORT).show();
                mAdapter.clear();

                Download_data download_data = new Download_data((download_complete) MainActivity.this);
                download_data.download_data_from_link(Url + Long.toString(new GetUserId().getUserId()) + "/Candidates");
                mAdapter.addAll(courseList);
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
        download_data.download_data_from_link(Url + Long.toString(new GetUserId().getUserId()) + "/Candidates");

        mListView.setItemsCanFocus(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Show subpage
                Intent intent = new Intent();
                intent.putExtra("id",mAdapter.getIdbyPosition(position));
                intent.putExtra("title",mAdapter.getTitlebyPosition(position));
                intent.putExtra("description",mAdapter.getDiscriptionbyPosition(position));

                intent.setClass(MainActivity.this,DetailActivity.class);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Click to subpage! ",Toast.LENGTH_SHORT).show();

                //Send post to server
                String courseId = MainActivity.this.mAdapter.getIdbyPosition(position);
                Runnable networkTask = new NetworkThread(courseId, 3);
                new Thread(networkTask).start();

                //Toast.makeText(getApplicationContext(), url,Toast.LENGTH_SHORT).show();
            }


        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void LoadData()
    {
        // start to load
        Toast.makeText(getApplicationContext(), "加载更多", Toast.LENGTH_SHORT).show();

        textMore.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Download_data download_data = new Download_data((download_complete) MainActivity.this);
        download_data.download_data_from_link(Url + Long.toString(new GetUserId().getUserId()) + "/Candidates");
        mAdapter.addAll(courseList);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "刷新完成!", Toast.LENGTH_SHORT).show();
                //swipeContainer.setRefreshing(false);
                mRefreshLayout.setLoading(false);
                mAdapter.notifyDataSetChanged();
                textMore.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 4000);

    }

    public void get_data(String data)
    {
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
                map.put(KEY_URL,obj.getString("courselink"));
                courseList.add(map);

            }
            mAdapter.notifyDataSetChanged();

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
