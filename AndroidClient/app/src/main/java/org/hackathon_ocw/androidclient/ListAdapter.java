package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dianyang on 2016/2/28.
 */
public class ListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public ListAdapter(Activity a, ArrayList<HashMap<String, String>> d){
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return  data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getVideoUrlbyPosition(int position) {
        return data.get(position).get("videoUrl");
    }

    public String getTitlebyPosition(int position) {
        return data.get(position).get("title");
    }

    public String getIdbyPosition(int position) {
        if(data.size() <= position)
        {
            return  data.get(position-1).get("id");
        }
        else
        {
            return data.get(position).get("id");
        }
    }

    public String getDiscriptionbyPosition(int position) {
        return data.get(position).get("description");
    }

    public String getVideoImgbyPosition(int position){
        return data.get(position).get("thumb_url");
    }

    public void clear()
    {
        data.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<HashMap<String, String>> d)
    {
        data = d;
        notifyDataSetChanged();
    }

    public void append(ArrayList<HashMap<String, String>> d)
    {
        data.addAll(d);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View vi = convertView;
        if(convertView == null)
        {
            vi = inflater.inflate(R.layout.cell,null);
        }

        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView videoDurationText = (TextView) vi.findViewById(R.id.videoDurationText);
        TextView videoWatchedText = (TextView) vi.findViewById(R.id.videoWatchedText);
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.pic_link);


        HashMap<String, String> course = new HashMap<String, String>();
        course = data.get(position);

        title.setText(course.get(MainActivity.KEY_TITLE));
        videoDurationText.setText(course.get(MainActivity.KEY_DURATION));
        imageLoader.DisplayImage(course.get(MainActivity.KEY_THUMB_URL), thumb_image);

        //Will be replaced by real data
        //videoDurationText.setText("03:59");
        //videoWatchedText.setText("网易公开课");
        videoWatchedText.setText(course.get(MainActivity.KEY_SOURCE));

        return vi;
    }

}
