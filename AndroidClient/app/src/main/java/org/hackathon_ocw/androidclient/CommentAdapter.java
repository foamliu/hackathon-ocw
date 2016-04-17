package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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

    //Add new comments to front
    public void AddComments(HashMap<String, String> d){
        data.add(0, d);
        notifyDataSetChanged();
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
        CircularImage userImageView = (CircularImage) vi.findViewById(R.id.userCommentHeadImage);
        TextView comment = (TextView)vi.findViewById(R.id.comment);
        TextView commentTime = (TextView)vi.findViewById(R.id.commentTime);
        TextView like = (TextView)vi.findViewById(R.id.like);

        HashMap<String, String> comments = new HashMap<String, String>();
        comments = data.get(position);

        //Add a null hint
        if(comments == null)
        {
            TextView tv=(TextView)vi;
            tv.setText("还没有笔记，留下所思所感吧");
            tv.setGravity(Gravity.CENTER);
        }

        addListenerOnLikeButton(vi, comments);

        userName.setText(comments.get(TabComment.KEY_USERNAME));
        comment.setText(comments.get(TabComment.KEY_COMMENT));
        like.setText(comments.get(TabComment.KEY_LIKE));
        String headimgurl = comments.get(TabComment.KEY_USERIMAGE);

        if(headimgurl != null)
        {
            RequestQueue mQueue = Volley.newRequestQueue(activity.getApplicationContext());
            com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
            com.android.volley.toolbox.ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(userImageView,R.drawable.no_image, R.drawable.no_image);
            imageLoader.get(headimgurl, listener);
        }

        //convert the time
        String commentTimeStr = comments.get(TabComment.KEY_COMMENTTIME);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
        if(currentTimeCal.get(Calendar.DATE) == commentTimeCal.get(Calendar.DATE))
        {
            if(diffDate < 24 && diffDate > 0)
            {
                commentTime.setText(diffDate + "小时前");
            }
        }
        else
        {
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM-dd HH:mm");
            commentTime.setText(simpleDateFormat1.format(commentTimeCal.getTime()));
        }
        return vi;
    }

    public void addListenerOnLikeButton(final View vi,final HashMap<String, String> comments){
        final ImageButton likeBtn = (ImageButton)vi.findViewById(R.id.likeBtn);
        likeBtn.setColorFilter(Color.parseColor("#64B5F6"));
        likeBtn.setAlpha((float) 0.5);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Back",Toast.LENGTH_SHORT).show();

                //Update the like num
                TextView like = (TextView)vi.findViewById(R.id.like);
                int comment = Integer.parseInt(comments.get(TabComment.KEY_LIKE)) + 1;
                like.setText(String.valueOf(comment));

                //Send GET request
                String id = comments.get(TabComment.KEY_COMMENT_ID);

                RequestQueue queue = Volley.newRequestQueue(vi.getContext());
                String url ="http://jieko.cc/item/Comments/" + id + "/like";

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d("Response", response.toString());
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error.Response", error.toString());
                            }
                        }
                );
                //Change the color
                likeBtn.setColorFilter(Color.parseColor("#1565C0"));
            }
        });
    }
}
