package org.hackathon_ocw.androidclient.widget;

/**
 * Created by Foam on 2016/5/22.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.activity.DetailActivity;
import org.hackathon_ocw.androidclient.activity.WebDetailActivity;
import org.hackathon_ocw.androidclient.adapter.ListAdapter;
import org.hackathon_ocw.androidclient.domain.Course;
import org.hackathon_ocw.androidclient.domain.UserProfile;
import org.hackathon_ocw.androidclient.util.Constants;
import org.hackathon_ocw.androidclient.util.CustomApplication;
import org.hackathon_ocw.androidclient.util.Downloader;
import org.hackathon_ocw.androidclient.util.NetworkThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsFragment extends Fragment
        implements Downloader.download_complete {

    private Tracker mTracker;
    private static final String ARG_POSITION = "position";
    private static final String TAG = "NewsFragment";
    private static final String BaseUrl = "http://api.jieko.cc/user/";

    private String catalog;

    private RefreshLayout mRefreshLayout;
    private ArrayList<HashMap<String, String>> courseList = new ArrayList<>();
    private ListAdapter mListAdapter;

    private int positionYixi; //TODO

    public static NewsFragment newInstance(int position) {
        NewsFragment f = new NewsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int position = getArguments().getInt(ARG_POSITION);
        this.catalog = Constants.catalogs.get(position);

        CustomApplication application = (CustomApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Catalog~" + catalog);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        FrameLayout fl = new FrameLayout(getActivity());
        fl.setLayoutParams(params);

        View vi;
        vi = inflater.inflate(R.layout.content_main, container, false);
        listViewInit(vi);
        fl.addView(vi);
        return fl;
    }

    private void listViewInit(View vi) {
        ListView mListView = (ListView) vi.findViewById(R.id.list);
        mRefreshLayout = (RefreshLayout) vi.findViewById(R.id.swipeContainer);
        mRefreshLayout.setChildView(mListView);
        mListAdapter = new ListAdapter(getActivity(), courseList);
        mListView.setAdapter(mListAdapter);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadingData();
            }
        });

        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                startLoadingData();
            }
        });

        startLoadingData();

        mListView.setItemsCanFocus(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    boolean isYixi = false;

                    if (mListAdapter.getWebUrlByPosition(position).contains("yixi")) {
                        parseYixiCourseStep1(mListAdapter.getWebUrlByPosition(position));
                        positionYixi = position;
                        isYixi = true;
                    }

                    long itemId = Long.valueOf(mListAdapter.getIdByPosition(position));
                    String title = mListAdapter.getTitleByPosition(position);
                    String description = mListAdapter.getDescriptionByPosition(position);
                    String thumbUrl = mListAdapter.getVideoImgByPosition(position);
                    String videoUrl = mListAdapter.getVideoUrlByPosition(position);
                    String webUrl = mListAdapter.getWebUrlByPosition(position);
                    String userId = UserProfile.getInstance().getUserId();

                    if (!"".equals(videoUrl) && !isYixi) {
                        //Show subpage with videoUrl
                        Intent intent = new Intent();
                        intent.putExtra("id", String.valueOf(itemId));
                        intent.putExtra("title", title);
                        intent.putExtra("videoUrl", videoUrl);
                        intent.putExtra("description", description);
                        intent.putExtra("videoImg", thumbUrl);
                        intent.putExtra("userid", userId);
                        if (UserProfile.getInstance().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                        }

                        intent.setClass(getActivity(), DetailActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (mListAdapter.getVideoUrlByPosition(position).equals("") && !isYixi) {
                        //Show subpage with Webview
                        Intent intent = new Intent();
                        intent.putExtra("webUrl", webUrl);
                        intent.putExtra("id", String.valueOf(itemId));
                        intent.putExtra("title", title);
                        intent.putExtra("userid", userId);
                        if (UserProfile.getInstance().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                        }

                        intent.setClass(getActivity(), WebDetailActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    //Send post to server
                    Course course = new Course(itemId,title,description,thumbUrl,videoUrl,webUrl);
                    Runnable networkTask = new NetworkThread(UserProfile.getInstance().getUserId(), course, 3);
                    new Thread(networkTask).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Send event to Google Analytics
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Mainpage:" + catalog)
                        .setAction("Click the ocw item")
                        .setLabel(mListAdapter.getIdByPosition(position))
                        .setValue(1)
                        .build());
            }
        });
    }

    @NonNull
    private String getUrl() {
        String url = BaseUrl + UserProfile.getInstance().getUserId() + "/Candidates";
        if (!Constants.StringRecommend.equals(NewsFragment.this.catalog))
        {
            String tag = "";
            try {
                tag = URLEncoder.encode(catalog, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url += "/tag/" + tag;
        }
        return url;
    }

    private void startLoadingData() {
        // start to load
        //Toast.makeText(getActivity(), "玩命加载中...", Toast.LENGTH_SHORT).show();
        courseList.clear();
        Downloader downloader = new Downloader(NewsFragment.this);
        downloader.download_data_from_link(getUrl());
    }

    @Override
    public void onDataLoaded(String data) {
        try {
            JSONObject object = new JSONObject(data);
            JSONArray data_array = object.getJSONArray("courses");
            for (int i = 0; i < data_array.length(); i++) {
                JSONObject obj = new JSONObject(data_array.get(i).toString());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Constants.KEY_ID, String.valueOf(obj.getInt("item_id")));
                map.put(Constants.KEY_TITLE, obj.getString("title"));
                map.put(Constants.KEY_DESCRIPTION, obj.getString("description"));
                map.put(Constants.KEY_THUMB_URL, obj.getString("piclink"));
                map.put(Constants.KEY_VIDEOURL, obj.getString("courselink"));
                map.put(Constants.KEY_WEBURL, obj.getString("link"));
                map.put(Constants.KEY_DURATION, obj.getString("duration"));
                map.put(Constants.KEY_SOURCE, obj.getString("source"));
                map.put(Constants.KEY_INSTRUCTOR, obj.getString("instructor"));
                map.put(Constants.KEY_LANGUAGE, obj.getString("language"));
                map.put(Constants.KEY_SCHOOL, obj.getString("school"));
                map.put(Constants.KEY_TAGS, obj.getString("tags"));
                courseList.add(map);
            }

            mListAdapter.notifyDataSetChanged();
            mRefreshLayout.setLoading(false);
            mRefreshLayout.setRefreshing(false);
            //Toast.makeText(getActivity(), "推荐引擎有20条更新", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parseYixiCourseStep1(String videoUrl) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, videoUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("vid")) {
                            Pattern pattern = Pattern.compile("(?<=vid: \').*(?=\')");
                            Matcher matcher = pattern.matcher(response);
                            if (matcher.find()) {
                                parseYixiCourseStep2(matcher.group(0));
                                //Log.e("Get regex", matcher.group(0));
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Get error", error.toString());
            }
        });
        queue.add(stringRequest);
    }

    public void parseYixiCourseStep2(String token) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String url = "http://api.yixi.tv/youku.php?id=" + token;
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONObject("files").getJSONObject("3gphd").getJSONArray("segs");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String link = jsonObject.getString("url").replace("\\", "");

                            //Show subpage with videoUrl
                            Intent intent = new Intent();
                            intent.putExtra("id", mListAdapter.getIdByPosition(positionYixi));
                            intent.putExtra("title", mListAdapter.getTitleByPosition(positionYixi));
                            intent.putExtra("videoUrl", link);
                            intent.putExtra("description", mListAdapter.getDescriptionByPosition(positionYixi));
                            intent.putExtra("videoImg", mListAdapter.getVideoImgByPosition(positionYixi));
                            intent.putExtra("userid", UserProfile.getInstance().getUserId());
                            if (UserProfile.getInstance().getNickname() != null) {
                                intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                                intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                            }

                            intent.setClass(getActivity(), DetailActivity.class);
                            startActivity(intent);
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