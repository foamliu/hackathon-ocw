package org.hackathon_ocw.androidclient;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tencent.mm.sdk.openapi.IWXAPI;

import org.hackathon_ocw.androidclient.FullscreenVideoLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class DetailActivity extends AppCompatActivity   {

    private IWXAPI api;
    private VideoView videoView;
    private MediaController mediaController;

    private FullscreenVideoLayout videoLayout;

    private Uri uri;
    private Toolbar detailToolbar;
    private TextView titleDetail;
    private TextView titleToolBar;
    //private TextView descriptionDetail;
    private ViewPager viewPager;
    private RatingBar ratingBar;
    private Button backBtn;
    private Button shareBtn;
    private PopupWindow popWindow;
    private InputMethodManager imm;
    private EditText editText;

    private String courseId;
    private String description;
    private String title;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomApplication application = (CustomApplication) getApplication();
        mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        courseId = intent.getStringExtra("id");
        uri = Uri.parse(intent.getStringExtra("videoUrl"));

        detailToolBarInit();

        titleDetail=(TextView)findViewById(R.id.titleDetail);
        titleDetail.setText(title);

        videoInit();
        viewPagerInit();
        addListenerOnBackButton();
        addListenerOnShareButton();


        //addListenerOnCommentButton();
        //addListenerOnViewCommentButton();
        //addListenerOnFavoritesButton();


        addListenerOnRatingBar();

        //Google Analytics tracker
        sendScreenImageName();

    }
        // Toast.makeText(this, result, Toast.LENGTH_LONG).show();

    public void viewPagerInit(){
        viewPager = (ViewPager) findViewById(R.id.detailPager);
        viewPager.setAdapter(new PageFragmentAdapter(getSupportFragmentManager(),
                DetailActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.detailTabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
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

    public void videoInit(){
        /*
        videoView=(VideoView)findViewById(R.id.videoView);
        mediaController=new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.requestFocus();
        */

        videoLayout = (FullscreenVideoLayout)findViewById(R.id.videoView);
        videoLayout.setActivity(this);
        videoLayout.setShouldAutoplay(false);
        try{
            videoLayout.setVideoURI(uri);
        }catch (IOException e)
        {
            e.printStackTrace();
            Log.e("videoLayout", e.toString());
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        videoLayout.resize();
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
                //Toast.makeText(getApplicationContext(), "Back",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void addListenerOnShareButton() {
        //Share to Wechat
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String text = "我正在学啥的公开课: " + title;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                /*
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
                */
            }
        });
    }

    public void addListenerOnViewCommentButton(){
        //Change view
        Button viewComment = (Button)findViewById(R.id.ViewCommentBtn);
        viewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1, true);
            }
        });
    }

    /*
    public void addListenerOnFavoritesButton(){
        //Add this course to favorites
        ImageButton favorites = (ImageButton)this.findViewById(R.id.FavoritesBtn);
        favorites.setColorFilter(Color.YELLOW);
        favorites.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Format jsonObject
                JSONObject jsonObject = new JSONObject();
                try
                {
                    jsonObject.put("courseId", courseId);
                    jsonObject.put("title", title);
                }catch (Exception e)
                {
                    Log.e("Local file Error",e.toString());
                }
                //Save to local stroage
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("favorites.json", Context.MODE_PRIVATE));
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.close();
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        });
        /*
        favorites.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                    //Change Favorites icon color
                    favorites.setColorFilter(Color.YELLOW);
                    return true;
                }
                return false;
            }
        });


    }
    */

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

    public void addListenerOnCommentButton(){
        editText = (EditText)findViewById(R.id.EditComment);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //commentShowPopup(v);
                    //popUpInputMethodWindow();
                } else {

                }
            }
        });
    }

    public void commentShowPopup(final View parent){

        if(popWindow == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.comment_popwindow, null);
            popWindow = new PopupWindow(view, LinearLayout.LayoutParams.FILL_PARENT, 80, true);
        }
        else
        {
            popWindow.update();
        }
        //popWindow.setAnimationStyle(R.style.pop);
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(false);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        addListenerOnSendCommentButton();

        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popDownInputMethodWindow(parent);
            }
        });

        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                return false;
            }
        });
    }

    private void popUpInputMethodWindow(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imm = (InputMethodManager) editText.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 0);
    }

    private void popDownInputMethodWindow(final View view){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imm = (InputMethodManager) editText.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }, 0);
    }

    private void addListenerOnSendCommentButton(){
        ImageButton sendCommentBtn = (ImageButton)popWindow.getContentView().findViewById(R.id.SendBtn);
        sendCommentBtn.setColorFilter(Color.parseColor("#64B5F6"));
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Get item_id
                int item_id = Integer.valueOf(courseId);

                //Fake the item_id
                item_id = 1;

                //Get author_id (faked)
                int author_id = 1;
                String author_name = "习近平";
                int like = 0;

                //Get post time
                Calendar currentTime = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String currentTimeStr = simpleDateFormat.format(currentTime.getTime());

                //Get text
                EditText editText = (EditText)popWindow.getContentView().findViewById(R.id.WriteCommentPopWin);
                String comment = editText.getText().toString();

                //Get current timeline
                VideoView videoView = (VideoView)findViewById(R.id.videoView);
                int timeline = (videoView.getCurrentPosition()) / 1000;

                //Get Url
                //String httpurl = "http://jieko.cc/item/" + courseId + "/Comments";
                //For debug
                String httpurl = "http://jieko.cc/item/1/Comments";

                //Send a POST message
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                JSONObject jsonObject = new JSONObject();
                try
                {
                    jsonObject.put("item_id", item_id);
                    jsonObject.put("author_id", author_id);
                    jsonObject.put("author_name", author_name);
                    jsonObject.put("posted", currentTimeStr);
                    jsonObject.put("text", comment);
                    jsonObject.put("timeline", timeline);
                    jsonObject.put("like", like);

                }catch (Exception e)
                {
                    Log.e("Json Error",e.toString());
                }
                JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST,httpurl, jsonObject,
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

                //Update the comment list

                HashMap<String, String>map = new HashMap<String, String>();
                map.put("commentId", String.valueOf(item_id));
                //map.put("author_id", String.valueOf(author_id));
                map.put("userName", author_name);
                map.put("commentTime", currentTimeStr);
                map.put("comment", comment);
                map.put("timeline", String.valueOf(timeline));
                map.put("like", String.valueOf(like));

                PageFragmentAdapter pageFragmentAdapter = (PageFragmentAdapter)viewPager.getAdapter();
                pageFragmentAdapter.getTabComment().mCommentAdapter.AddComments(map);

                popWindow.dismiss();
            }
        });

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