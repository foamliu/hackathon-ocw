package org.hackathon_ocw.androidclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.hackathon_ocw.androidclient.FullscreenVideoLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by dianyang on 2016/4/19.
 */
public class WebDetailActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private IWXAPI api;
    private Toolbar detailToolbar;
    private TextView titleDetail;
    private TextView titleToolBar;

    private Button backBtn;
    private Button shareBtn;

    private PopupWindow popWindow;
    private InputMethodManager imm;
    private EditText editText;

    private String webUri;
    private String courseId;
    private String title;

    //User info
    private UserProfile userProfile;
    private Tracker mTracker;

    private WebView browser= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomApplication application = (CustomApplication) getApplication();
        mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_webdetail);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);

        userProfile = new UserProfile();

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        courseId = intent.getStringExtra("id");
        webUri = intent.getStringExtra("webUrl");

        userProfile.setNickname(intent.getStringExtra("nickname"));
        userProfile.setHeadimgurl(intent.getStringExtra("headimgurl"));
        userProfile.setUserid(intent.getStringExtra("userid"));

        detailToolBarInit();

        addListenerOnBackButton();
        addListenerOnShareButton();

        loadWebview();

        //addListenerOnCommentButton();
        //addListenerOnViewCommentButton();
        //addListenerOnRatingBar();

        //Google Analytics tracker
        sendScreenImageName();
        //mChildHelper = new NestedScrollingChildHelper(findViewById(R.layout.activity_detail));
    }

    public void detailToolBarInit(){
        detailToolbar = (Toolbar) findViewById(R.id.detailToolbar);
        setSupportActionBar(detailToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        detailToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);

        titleToolBar=(TextView)findViewById(R.id.titleToolBar);
        titleToolBar.setText("学啥");
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

    public void addListenerOnBackButton() {
        backBtn = (Button)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPause();
                finish();
            }
        });
    }

    public void addListenerOnShareButton() {
        //Share to Wechat
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(WebDetailActivity.this, v);
                popupMenu.setOnMenuItemClickListener(WebDetailActivity.this);
                popupMenu.inflate(R.menu.main);
                popupMenu.show();

            }
        });
    }

    public boolean onMenuItemClick(MenuItem item){
        switch (item.getItemId()){
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

    public void WXShare(boolean isTimelineCb){
        //WXVideoObject videoObject = new WXVideoObject();
        //videoObject.videoUrl = uri.toString();

        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = webUri.toString();

        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        msg.title = title;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void loadWebview(){
        if(webUri != ""){
            browser=(WebView)findViewById(R.id.webview);
            browser.getSettings().setJavaScriptEnabled(true);
            browser.loadUrl(webUri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        browser.onPause();
    }

    //Google Analytics
    private void sendScreenImageName() {
        String name = title;
        // [START screen_view_hit]
        //Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Web~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]
    }
}
