package org.hackathon_ocw.androidclient.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import org.hackathon_ocw.androidclient.activity.DownloadListActivity;
import org.hackathon_ocw.androidclient.util.Constants;
import org.hackathon_ocw.androidclient.util.ImageLoader;
import org.hackathon_ocw.androidclient.util.StorageUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dianyang on 2016/2/28.
 */
public class ListAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> data;
    private final LayoutInflater inflater;
    public final ImageLoader imageLoader;
    private final Context appContext;
    private int positionYixi; //TODO

    public ListAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
        this.data = data;
        this.appContext = activity.getApplicationContext();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getVideoUrlByPosition(int position) {
        return data.get(position).get("videoUrl");
    }

    public String getWebUrlByPosition(int position) {
        return data.get(position).get("webUrl");
    }

    public String getTitleByPosition(int position) {
        return data.get(position).get("title");
    }

    public String getIdByPosition(int position) {
        if (data.size() <= position) {
            return data.get(position - 1).get("id");
        } else {
            return data.get(position).get("id");
        }
    }

    public String getDescriptionByPosition(int position) {
        return data.get(position).get("description");
    }

    public String getVideoImgByPosition(int position) {
        return data.get(position).get("thumb_url");
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void addAll(int index, ArrayList<HashMap<String, String>> d) {
        data.addAll(index, d);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<HashMap<String, String>> d) {
        data.addAll(d);
        notifyDataSetChanged();
    }

    public void append(ArrayList<HashMap<String, String>> d) {
        data.addAll(d);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi;
        if (convertView != null)
            vi = convertView;
        else {
            vi = inflater.inflate(R.layout.cell, parent, false);
        }

        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView duration = (TextView) vi.findViewById(R.id.duration);
        TextView school = (TextView) vi.findViewById(R.id.school);
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.pic_link);
        ImageView downloadBtn = (ImageView) vi.findViewById(R.id.downloadBtn);

        HashMap<String, String> course = data.get(position);
        final int iPosition = position;
        final String strItemId = this.getIdByPosition(position);
        final String strTitle = course.get(Constants.KEY_TITLE);
        final String strDescription = course.get(Constants.KEY_DESCRIPTION);
        String strSchool = course.get(Constants.KEY_SCHOOL);
        if (strSchool.length() > 12)
            strSchool = strSchool.substring(0, 12) + "..";
        String strDuration = course.get(Constants.KEY_DURATION);
        if (strDuration == null) {
            strDuration = "";
        }
        final String strThumbUrl = course.get(Constants.KEY_THUMB_URL);
        final String strVideoUrl = course.get(Constants.KEY_VIDEOURL);
        final String strWebUrl = course.get(Constants.KEY_WEBURL);

        title.setText(strTitle);
        school.setText(strSchool);
        duration.setText(strDuration);

        imageLoader.DisplayImage(strThumbUrl, thumb_image);

        if (strVideoUrl != null && !strVideoUrl.trim().equals("")) {
            if (StorageUtils.isDownloaded(Long.valueOf(strItemId))) {
                downloadBtn.setImageResource(R.drawable.ic_correct_32dp);
                downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(appContext, DownloadListActivity.class);
                        appContext.startActivity(intent);
                    }
                });
            } else {
                downloadBtn.setVisibility(View.VISIBLE);
                downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.Self, "加入下载列表: " + strVideoUrl, Toast.LENGTH_SHORT).show();
                        //Show subpage with videoUrl
                        Intent intent = new Intent();
                        intent.putExtra(Constants.KEY_ID, strItemId);
                        intent.putExtra(Constants.KEY_TITLE, strTitle);
                        intent.putExtra(Constants.KEY_DESCRIPTION, strDescription);
                        intent.putExtra(Constants.KEY_THUMB_URL, strThumbUrl);
                        intent.putExtra(Constants.KEY_VIDEOURL, strVideoUrl);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setClass(appContext, DownloadListActivity.class);
                        appContext.startActivity(intent);
                    }
                });
            }
        } else if (strWebUrl.contains("yixi")) {
            downloadBtn.setVisibility(View.VISIBLE);
            downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parseYixiCourseStep1(ListAdapter.this.getWebUrlByPosition(iPosition));
                    positionYixi = iPosition;
                }
            });
        } else {
            downloadBtn.setVisibility(View.INVISIBLE);
        }

        return vi;
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

                            Intent intent = new Intent();

                            intent.putExtra(Constants.KEY_ID, ListAdapter.this.getIdByPosition(positionYixi));
                            intent.putExtra(Constants.KEY_TITLE, ListAdapter.this.getTitleByPosition(positionYixi));
                            intent.putExtra(Constants.KEY_DESCRIPTION, ListAdapter.this.getDescriptionByPosition(positionYixi));
                            intent.putExtra(Constants.KEY_THUMB_URL, ListAdapter.this.getVideoImgByPosition(positionYixi));
                            intent.putExtra(Constants.KEY_VIDEOURL, link);

                            intent.setClass(appContext, DownloadListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
