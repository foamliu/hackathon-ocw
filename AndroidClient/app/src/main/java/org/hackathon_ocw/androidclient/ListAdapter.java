package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dianyang on 2016/2/28.
 */
public class ListAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> data;
    private final LayoutInflater inflater;
    public final ImageLoader imageLoader;
    private final Context appContext;

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

    public void addAll(ArrayList<HashMap<String, String>> d) {
        data = d;
        notifyDataSetChanged();
    }

    public void append(ArrayList<HashMap<String, String>> d) {
        data.addAll(d);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = null;
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
        if (duration == null || duration.equals("")) {
            strDuration = "";
        }
        final String strThumbUrl = course.get(Constants.KEY_THUMB_URL);
        final String strVideoUrl = course.get(Constants.KEY_VIDEOURL);

        title.setText(strTitle);
        school.setText(strSchool);
        duration.setText(strDuration);

        imageLoader.DisplayImage(strThumbUrl, thumb_image);

        if (strVideoUrl == null || strVideoUrl.equals("") || strVideoUrl.trim().equals("")) {
            downloadBtn.setVisibility(View.INVISIBLE);
        } else {
            downloadBtn.setVisibility(View.VISIBLE);
            downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(MainActivity.Self, "加入下载列表: " + strVideoUrl, Toast.LENGTH_SHORT).show();

                    //Show subpage with videoUrl
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constants.KEY_ID, strItemId);
                    intent.putExtra(Constants.KEY_TITLE, strTitle);
                    intent.putExtra(Constants.KEY_DESCRIPTION, strDescription);
                    intent.putExtra(Constants.KEY_THUMB_URL, strThumbUrl);
                    intent.putExtra(Constants.KEY_VIDEOURL, strVideoUrl);

                    intent.setClass(appContext, DownloadListActivity.class);
                    appContext.startActivity(intent);
                }
            });
        }

        return vi;
    }

}
