package org.hackathon_ocw.androidclient.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.activity.DetailActivity;
import org.hackathon_ocw.androidclient.activity.WebDetailActivity;
import org.hackathon_ocw.androidclient.domain.Course;
import org.hackathon_ocw.androidclient.domain.HistoryEntry;
import org.hackathon_ocw.androidclient.domain.UserProfile;
import org.hackathon_ocw.androidclient.util.Constants;
import org.hackathon_ocw.androidclient.util.ImageLoader;
import org.hackathon_ocw.androidclient.util.NetworkThread;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Foam on 2016/6/4.
 */
public class HistoryListAdapter extends BaseAdapter {

    private final static String TAG = "HistoryListAdapter";
    private final Context appContext;
    private List<HistoryEntry> dataList;
    private final ImageLoader imageLoader;
    private int positionYixi;

    public HistoryListAdapter(Activity activity) {
        this.appContext = activity.getApplicationContext();
        this.dataList = new ArrayList<>();
        this.imageLoader = new ImageLoader(activity.getApplicationContext());
        init();
    }

    private void init() {
        this.loadData();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi;
        if (convertView != null)
            vi = convertView;
        else {
            vi = LayoutInflater.from(appContext).inflate(R.layout.history_list_item, parent, false);
        }

        final HistoryEntry entry = dataList.get(position);

        final long itemId = entry.course.getItemid();
        final String strTitle = entry.course.getTitle();
        final String description = entry.course.getDescription();
        final String thumbUrl = entry.course.getPiclink();
        final String videoUrl = entry.course.getCourselink();
        final String webUrl = entry.course.getWebUrl();
        final String userId = UserProfile.getInstance().getUserId();

        ImageView thumbImage = (ImageView) vi.findViewById(R.id.pic_link);
        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView watchedTime = (TextView) vi.findViewById(R.id.watchedTime);

        imageLoader.DisplayImage(entry.course.getPiclink(), thumbImage);
        title.setText(strTitle);
        watchedTime.setText(entry.watchedTime);

        RelativeLayout relative =  (RelativeLayout) vi.findViewById(R.id.history_list_item);
        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean isYixi = false;

                    if (webUrl.contains("yixi")) {
                        parseYixiCourseStep1(webUrl);
                        positionYixi = position;
                        isYixi = true;
                    }

                    if (!"".equals(videoUrl) && !isYixi) {
                        //Show subpage with videoUrl
                        Intent intent = new Intent();
                        intent.putExtra("id", String.valueOf(itemId));
                        intent.putExtra("title", strTitle);
                        intent.putExtra("videoUrl", videoUrl);
                        intent.putExtra("description", description);
                        intent.putExtra("videoImg", thumbUrl);
                        intent.putExtra("userid", userId);
                        if (UserProfile.getInstance().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.setClass(appContext, DetailActivity.class);
                        appContext.startActivity(intent);
                    } else if (videoUrl.equals("") && !isYixi) {
                        //Show subpage with Webview
                        Intent intent = new Intent();
                        intent.putExtra("webUrl", webUrl);
                        intent.putExtra("id", String.valueOf(itemId));
                        intent.putExtra("title", strTitle);
                        intent.putExtra("userid", userId);
                        if (UserProfile.getInstance().getNickname() != null) {
                            intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                            intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.setClass(appContext, WebDetailActivity.class);
                        appContext.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        return vi;
    }

    private void loadData() {
        this.dataList = UserProfile.getInstance().getHistory();
        Collections.sort(this.dataList, new Comparator<HistoryEntry>(){
            @Override
            public int compare(HistoryEntry lhs, HistoryEntry rhs) {
                try {
                    Date d1 = Constants.DateFormat.parse(lhs.watchedTime);
                    Date d2 = Constants.DateFormat.parse(rhs.watchedTime);
                    return (int)(d2.getTime() - d1.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    public void parseYixiCourseStep1(String videoUrl) {
        RequestQueue queue = Volley.newRequestQueue(appContext);
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
        RequestQueue requestQueue = Volley.newRequestQueue(appContext);
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
                            HistoryEntry entry = dataList.get(positionYixi);
                            intent.putExtra("id", String.valueOf(entry.course.getItemid()));
                            intent.putExtra("title", entry.course.getTitle());
                            intent.putExtra("videoUrl", link);
                            intent.putExtra("description", entry.course.getDescription());
                            intent.putExtra("videoImg", entry.course.getPiclink());
                            intent.putExtra("userid", UserProfile.getInstance().getUserId());
                            if (UserProfile.getInstance().getNickname() != null) {
                                intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                                intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                            }

                            intent.setClass(appContext, DetailActivity.class);
                            appContext.startActivity(intent);
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
