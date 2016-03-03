package org.hackathon_ocw.androidclient;

import org.hackathon_ocw.androidclient.Download_data.download_complete;

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
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, download_complete {

    public ListView list;
    public ArrayList<HashMap<String, String>> courseList = new ArrayList<HashMap<String, String>>();
    public ListAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    static final String KEY_ID = "0";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_THUMB_URL = "thumb_url";
    static final String KEY_URL = "url";
    static final String Url = "http://40.83.121.223/Candidates";
    static final String postUrl = "http://40.83.121.223/Preferences";
    static final float rating = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //fetchTimeLineAsync(0);
                Toast.makeText(getApplicationContext(), "正在刷新... ",Toast.LENGTH_SHORT).show();
                adapter.clear();

                Download_data download_data = new Download_data((download_complete)MainActivity.this);
                download_data.download_data_from_link(Url);
                adapter.addAll(courseList);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "刷新完成!",Toast.LENGTH_SHORT).show();
                        swipeContainer.setRefreshing(false);
                    }
                }, 4000);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright);

        list = (ListView) findViewById(R.id.list);
        adapter = new ListAdapter(this, courseList);
        list.setAdapter(adapter);

        final Download_data download_data = new Download_data((download_complete) this);
        download_data.download_data_from_link(Url);

        list.setItemsCanFocus(true);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String url = MainActivity.this.adapter.getUrlbyPosition(position);
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                //Send post to server
                String courseId = MainActivity.this.adapter.getIdbyPosition(position);
                String ipAddress = getLocalIPAddress();
                Toast.makeText(getApplicationContext(), ipAddress, Toast.LENGTH_SHORT).show();
                try {
                    //SendPostRequest(ipAddress, courseId, rating);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
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

    public void SendPostRequest(String userId, String itemId, float rating) throws Exception
    {
        String encoding="UTF-8";
        String params = "{\"user id\":" + userId + ",\"item id\"" + itemId + ",\"pref\"" + Double.toString(rating) + "\"}";
        URL url = new URL(postUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-javascript; charset=" + encoding);
        conn.setConnectTimeout(10000);
        byte[] data = params.toString().getBytes();
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    public String getLocalIPAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        // return inetAddress.getAddress().toString();
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("BaseScanTvDeviceClient", "Fetch ip false =" +  ex.toString());
        }
        return null;
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
                //map.put(KEY_ID,obj.getString("id"));
                map.put(KEY_TITLE,obj.getString("title"));
                map.put(KEY_DESCRIPTION,obj.getString("description"));
                map.put(KEY_THUMB_URL,obj.getString("pic_link"));
                map.put(KEY_URL,obj.getString("course_link"));
                courseList.add(map);

            }
            adapter.notifyDataSetChanged();

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

        if (id == R.id.nav_camera) {
            // Handle the camera action
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
