package org.hackathon_ocw.androidclient;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.hackathon_ocw.androidclient.Download_data.download_complete;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    VideoView videoView;
    MediaController mediaController;
    Uri uri = Uri.parse("http://mov.bn.netease.com/movie/2012/7/P/C/S854ATOPC.mp4");
    TextView titleDetail;
    TextView descriptionDetail;
    RatingBar ratingBar;
    Button backBtn;

    String courseId;
    String description;
    String title;

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

        descriptionDetail=(TextView)findViewById(R.id.descriptionDetail);
        descriptionDetail.setText(description);

        videoView=(VideoView)findViewById(R.id.videoView);
        mediaController=new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.requestFocus();

        backBtn = (Button)findViewById(R.id.backToMain);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Back",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        addListenerOnRatingBar();
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