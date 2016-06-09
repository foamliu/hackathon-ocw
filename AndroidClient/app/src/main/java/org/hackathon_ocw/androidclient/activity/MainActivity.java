package org.hackathon_ocw.androidclient.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.Tracker;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.domain.UserProfile;
import org.hackathon_ocw.androidclient.util.Constants;
import org.hackathon_ocw.androidclient.util.CustomApplication;
import org.hackathon_ocw.androidclient.util.Utils;
import org.hackathon_ocw.androidclient.widget.CategoryTabStrip;
import org.hackathon_ocw.androidclient.widget.NewsFragment;
import org.hackathon_ocw.androidclient.wxapi.WXEntryActivity;
import org.json.JSONObject;


public class MainActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener {

    private Tracker mTracker;
    private String access_token;
    private String openid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CategoryTabStrip tabs = (CategoryTabStrip) findViewById(R.id.category_strip);
        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        ImageView topHead = (ImageView) findViewById(R.id.top_head);
        ImageView btnExpand = (ImageView) findViewById(R.id.icon_expand);
        ImageView menu = (ImageView) findViewById(R.id.top_more);

        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        topHead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!UserProfile.getInstance().isLogin()) {
                    UserProfile.getInstance().WXLogin();
                }
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.main);
                popupMenu.show();
            }
        });
        btnExpand.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });


        if (checkNetworkStatus()) {
            UserProfile.init(this);

            // Obtain the shared Tracker instance.
            CustomApplication application = (CustomApplication) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Constants.catalogs.get(position);
        }

        @Override
        public int getCount() {
            return Constants.catalogs.size();
        }

        @Override
        public Fragment getItem(int position) {
            return NewsFragment.newInstance(position);
        }
    }

    public boolean checkNetworkStatus() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle("网络提示信息");
            builder.setMessage("网络不可用！");
            builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent;
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = new Intent();

        switch (item.getItemId()){
            case R.id.history:
                intent.setClass(this, HistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.download:
                intent.setClass(MainActivity.this, DownloadListActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }
}
