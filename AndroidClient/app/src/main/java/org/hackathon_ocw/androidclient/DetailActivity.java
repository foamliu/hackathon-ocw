package org.hackathon_ocw.androidclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;


import org.hackathon_ocw.androidclient.Download_data.download_complete;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailActivity extends AppCompatActivity{

    private IWXAPI api;
    private VideoView videoView;
    private MediaController mediaController;
    private Uri uri = Uri.parse("http://mov.bn.netease.com/movie/2012/7/P/C/S854ATOPC.mp4");
    private TextView titleDetail;
    private TextView titleToolBar;
    private TextView descriptionDetail;
    private RatingBar ratingBar;
    private Button backBtn;
    private Button shareBtn;

    private String courseId;
    private String description;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        courseId = intent.getStringExtra("id");

        titleDetail=(TextView)findViewById(R.id.titleDetail);
        titleDetail.setText(title);

        titleToolBar=(TextView)findViewById(R.id.titleToolBar);
        titleToolBar.setText("学啥");

        descriptionDetail=(TextView)findViewById(R.id.descriptionDetail);
        descriptionDetail.setText(description);

        videoView=(VideoView)findViewById(R.id.videoView);
        mediaController=new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.requestFocus();

        addListenerOnBackButton();
        addListenerOnShareButton();
        addListenerOnRatingBar();
    }
        // Toast.makeText(this, result, Toast.LENGTH_LONG).show();

    public void addListenerOnBackButton()
    {
        backBtn = (Button)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Back",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void addListenerOnShareButton()
    {
        //Share to Wechat
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Share to Wechat",Toast.LENGTH_SHORT).show();

                final EditText editor = new EditText(DetailActivity.this);
                editor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                editor.setText("我正在学啥的公开课: " + title);

                MMAlert.showAlert(DetailActivity.this, "send text", editor, "分享", "取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editor.getText().toString();
                        if (text == null || text.length() == 0) {
                            return;
                        }

                        WXTextObject textObj = new WXTextObject();
                        textObj.text = text;

                        WXMediaMessage msg = new WXMediaMessage();
                        msg.mediaObject = textObj;

                        // msg.title = "Will be ignored";
                        msg.description = text;


                        SendMessageToWX.Req req = new SendMessageToWX.Req();
                        req.transaction = buildTransaction("text");
                        req.message = msg;

                        //Share to friend
                        req.scene = 1;
                        //req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

                        api.sendReq(req);
                        finish();
                    }
                }, null);
            }
        });
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                Toast.makeText(getApplicationContext(), "Your ratings: " + String.valueOf(rating), Toast.LENGTH_SHORT).show();
                //txtRatingValue.setText(String.valueOf(rating));

                //Send the ratings to server...
                //Send post to server
                Runnable networkTask = new NetworkThread(courseId, rating);
                new Thread(networkTask).start();
            }
        });
    }

}