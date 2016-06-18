package org.hackathon_ocw.androidclient.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupMenu;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.domain.UserProfile;
import org.hackathon_ocw.androidclient.util.Constants;
import org.hackathon_ocw.androidclient.util.CustomApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by dianyang on 2016/4/19.
 */
public class WebDetailActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private IWXAPI api;
    private String webUri;
    private String title;
    private Tracker mTracker;
    private WebView browser= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomApplication application = (CustomApplication) getApplication();
        mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_webdetail);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);

        UserProfile userProfile = new UserProfile();

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        webUri = intent.getStringExtra("webUrl");

        userProfile.setNickname(intent.getStringExtra("nickname"));
        userProfile.setHeadimgurl(intent.getStringExtra("headimgurl"));
        userProfile.setUserId(intent.getStringExtra("userid"));

        addListenerOnBackButton();
        addListenerOnShareButton();

        loadWebview();

        //Google Analytics tracker
        sendScreenImageName();
    }

    public void addListenerOnBackButton() {
        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPause();
                finish();
            }
        });
    }

    public void addListenerOnShareButton() {
        //Share to Wechat
        Button shareBtn = (Button) findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(WebDetailActivity.this, v);
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
                popupMenu.setOnMenuItemClickListener(WebDetailActivity.this);
                popupMenu.inflate(R.menu.detail);
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
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = webUri;

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

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebview(){
        if(!webUri.equals("")){
            browser=(WebView)findViewById(R.id.webview);
            browser.getSettings().setJavaScriptEnabled(true);
            browser.getSettings().setUseWideViewPort(true);
            browser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            browser.getSettings().setLoadWithOverviewMode(true);
            browser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

            browser.setWebViewClient(new WebViewClient(){
                ProgressDialog progressDialog;

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
                @Override
                //Show loader on url load
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    if (progressDialog == null) {
                        // in standard case YourActivity.this
                        progressDialog = new ProgressDialog(WebDetailActivity.this);
                        progressDialog.setMessage("加载中...");
                        progressDialog.show();
                    }
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    try{
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }catch(Exception exception){
                        exception.printStackTrace();
                    }
                }
            });
            browser.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH); // for ＜ API　18
            if (Build.VERSION.SDK_INT >= 19) {
                // chromium, enable hardware acceleration
                browser.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                // older android version, disable hardware acceleration
                browser.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
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
