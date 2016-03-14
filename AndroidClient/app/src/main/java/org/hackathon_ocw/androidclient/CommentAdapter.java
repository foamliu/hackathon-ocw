package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by dianyang on 2016/3/14.
 */
public class CommentAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public CommentAdapter(Activity a, ArrayList<HashMap<String, String>> d){
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View vi = convertView;
        if(convertView == null)
        {
            vi = inflater.inflate(R.layout.comment,null);
        }

        //Username, UserImage, Comments, Timestamp, Like
        TextView userName = (TextView)vi.findViewById(R.id.userName);
        //ImageView userImage = (ImageView) vi.findViewById(R.id.userImage);
        TextView comment = (TextView)vi.findViewById(R.id.comment);
        TextView commentTime = (TextView)vi.findViewById(R.id.commentTime);
        TextView like = (TextView)vi.findViewById(R.id.like);

        HashMap<String, String> comments = new HashMap<String, String>();
        comments = data.get(position);

        userName.setText(comments.get(TabComment.KEY_USERNAME));
        comment.setText(comments.get(TabComment.KEY_COMMENT));

        like.setText(comments.get(TabComment.KEY_LIKE));
        //imageLoader.DisplayImage(comments.get(TabComment.KEY_USERIMAGE), userImage);

        //TODO: convert the time
        String commentTimeStr = comments.get(TabComment.KEY_COMMENTTIME);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar commentTimeCal = Calendar.getInstance();
        try{
            commentTimeCal.setTime(simpleDateFormat.parse(commentTimeStr));
        }catch (Exception e)
        {

            commentTimeCal = Calendar.getInstance();
        }

        Calendar currentTimeCal = Calendar.getInstance();
        long diffDate = currentTimeCal.get(Calendar.HOUR) - commentTimeCal.get(Calendar.HOUR);
        if(diffDate < 24 && diffDate != 0)
        {
            commentTime.setText(diffDate + "小时前");
        }
        else
        {
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM-dd HH:mm");
            commentTime.setText(simpleDateFormat1.format(commentTimeCal.getTime()));
        }

        return vi;
    }


}
