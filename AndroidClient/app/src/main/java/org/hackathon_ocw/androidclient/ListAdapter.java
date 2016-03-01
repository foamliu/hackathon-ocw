package org.hackathon_ocw.androidclient;

import org.hackathon_ocw.androidclient.R;

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

    public String getUrlbyPosition(int position) {
        return data.get(position).get("url");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View vi = convertView;
        if(convertView == null)
        {
            vi = inflater.inflate(R.layout.cell,null);
        }

        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView teacher = (TextView) vi.findViewById(R.id.teacher);
        TextView description = (TextView) vi.findViewById(R.id.description);
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.pic_link);


        HashMap<String, String> course = new HashMap<String, String>();
        course = data.get(position);

        title.setText(course.get(MainActivity.KEY_TITLE));
        teacher.setText(course.get(MainActivity.KEY_TEACHER));
        description.setText(course.get(MainActivity.KEY_DESCRIPTION));
        imageLoader.DisplayImage(course.get(MainActivity.KEY_THUMB_URL), thumb_image);

        return vi;
    }

}
