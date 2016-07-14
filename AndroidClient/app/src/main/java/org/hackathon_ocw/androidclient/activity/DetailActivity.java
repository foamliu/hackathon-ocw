package org.hackathon_ocw.androidclient.activity;


import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.adapter.PageFragmentAdapter;
import org.hackathon_ocw.androidclient.domain.UserProfile;
import org.hackathon_ocw.androidclient.util.Constants;
import org.hackathon_ocw.androidclient.util.CustomApplication;
import org.hackathon_ocw.androidclient.util.Utils;
import org.hackathon_ocw.androidclient.widget.FullscreenVideoLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DetailActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private IWXAPI api;
    private MediaController mediaController;

    private FullscreenVideoLayout videoLayout;

    private Uri uri;
    private ViewPager viewPager;
    private EditText editText;
    private Bitmap videoImage;

    private String courseId;
    private String description;
    private String title;

    private Tracker mTracker;

    private NestedScrollingChildHelper mChildHelper;
    private final DisplayMetrics metrics = new DisplayMetrics();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomApplication application = (CustomApplication) getApplication();
        mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detail);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        courseId = intent.getStringExtra("id");
        uri = Uri.parse(intent.getStringExtra("videoUrl"));
        UserProfile.getInstance().setNickname(intent.getStringExtra("nickname"));
        UserProfile.getInstance().setHeadimgurl(intent.getStringExtra("headimgurl"));
        UserProfile.getInstance().setUserId(intent.getStringExtra("userid"));
        String videoUrl = intent.getStringExtra("videoImg");
        String link = intent.getStringExtra("link");
        startRetrievingCourseInfo(link);

        TextView titleDetail = (TextView) findViewById(R.id.titleDetail);
        titleDetail.setText(title);
        TextView tabDescription = (TextView) findViewById(R.id.tabDescription);
        tabDescription.setText(description);

        getVideoImage(videoUrl);

        videoInit();
        addListenerOnBackButton();
        addListenerOnShareButton();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.videoLayout);
        videoLayout.getLayoutParams().height = metrics.widthPixels * 9 / 16;

        //Google Analytics tracker
        sendScreenImageName();
    }

    private void startRetrievingCourseInfo(String link)
    {
        String hash = Utils.md5(link);
        String url = "http://api.jieko.cc/course/item/" + hash;

        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        onRetrievedCourseInfo(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        queue.add(getRequest);
    }

    private void onRetrievedCourseInfo(JSONObject resp) {
        try {
            JSONArray courses = resp.getJSONArray("courses");
            if (courses.length() > 0) {
                JSONObject info = (JSONObject) courses.get(0);
                //final String coursetitle = info.getString("courseinstructor");
                final String coursedescription = info.getString("coursedescription");
                final String instructor = info.getString("courseinstructor");
                final JSONArray items = info.getJSONArray("items");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui
                        StringBuilder newDesc = new StringBuilder();
                        newDesc.append(String.format("讲师： %s\n\n", instructor));
                        newDesc.append(String.format("课程介绍： %s\n\n", coursedescription));
                        newDesc.append(String.format("本集内容： %s\n", description));
                        TextView tabDescription = (TextView) findViewById(R.id.tabDescription);
                        tabDescription.setText(newDesc.toString());


                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();

        //Get current timeline
        int position = videoLayout.getCurrentPosition();
        UserProfile.getInstance().setPosition(Long.valueOf(courseId), position);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int position = UserProfile.getInstance().getPosition(Long.valueOf(courseId));
        videoLayout.seekTo(position);
    }

    private void videoInit() {
        videoLayout = (FullscreenVideoLayout) findViewById(R.id.videoView);
        videoLayout.setActivity(this);
        videoLayout.setShouldAutoplay(true);
        try {
            videoLayout.setVideoURI(uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("videoLayout", e.toString());
        }
    }

    private void getVideoImage(String url) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                //给imageView设置图片
                videoImage = response;
            }
        }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        videoLayout.resize();
    }

    private void addListenerOnBackButton() {
        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addListenerOnShareButton() {
        //Share to Wechat
        Button shareBtn = (Button) findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(DetailActivity.this, v);
                //Use reflect to solve the issue that icon can't show in android 3.0+
                try {
                    Field[] fields = popupMenu.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popupMenu);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                    .getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod(
                                    "setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                popupMenu.setOnMenuItemClickListener(DetailActivity.this);
                popupMenu.inflate(R.menu.detail);
                popupMenu.show();

            }
        });
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shareWXSession:
                WXShare(true);
                return true;
            case R.id.shareWXTimeline:
                WXShare(false);
                return true;
            default:
                return false;
        }
    }

    private void WXShare(boolean isTimelineCb) {
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = uri.toString();

        //WXMediaMessage msg = new WXMediaMessage(videoObject);
        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        msg.title = title;
        msg.description = description;
        if (videoImage != null) {
            videoImage.getHeight();
            Bitmap thumb = Bitmap.createScaledBitmap(videoImage, 150, 120, true);
            msg.thumbData = Utils.bmpToByteArray(thumb);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //Google Analytics
    private void sendScreenImageName() {
        String name = title;
        // [START screen_view_hit]
        //Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]
    }
}