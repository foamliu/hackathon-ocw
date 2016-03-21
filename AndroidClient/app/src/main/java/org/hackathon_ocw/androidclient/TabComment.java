package org.hackathon_ocw.androidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by dianyang on 2016/3/14.
 */

public class TabComment extends Fragment implements Download_data.download_complete {

    public ListView mCommentView;
    public CommentAdapter mCommentAdapter;

    static final String KEY_USERNAME = "userName";
    static final String KEY_COMMENT = "comment";
    static final String KEY_COMMENT_ID = "commentId";
    static final String KEY_COMMENTTIME = "commentTime";
    static final String KEY_LIKE = "like";
    static final String KEY_USERIMAGE = "thumb_url";
    static final String KEY_TIMELINE = "timeline";
    static final String getCommentUrl = "http://jieko.cc/item/";

    public ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.tab_comment_detail, container, false);

        final Download_data download_data = new Download_data((Download_data.download_complete) this);
        download_data.download_data_from_link(getCommentUrl + "1" + "/Comments");

        mCommentView = (ListView) inflatedView.findViewById(R.id.commentList);
        mCommentAdapter = new CommentAdapter(getActivity(), commentList);
        mCommentView.setAdapter(mCommentAdapter);

        /*
        mCommentView.setOnScrollListener(new ListView.OnScrollChangeListener(){

            @Override
            public void OnScrollChange(View v, int l, int t, int oldl, int oldt){


            }
        });
        */

        return inflatedView;
    }

    @Override
    public void get_data(String data) {
        try {
            JSONArray data_array=new JSONArray(data);
            //JSONObject object = new JSONObject(data);
            //JSONArray data_array = object.getJSONArray("comments");
            for (int i = 0 ; i < data_array.length() ; i++)
            {
                JSONObject obj=new JSONObject(data_array.get(i).toString());

                HashMap<String, String> map = new HashMap<String,String>();
                //map.put(KEY_USERNAME,obj.getString("userName"));
                map.put(KEY_USERNAME,obj.getString("author_name"));
                map.put(KEY_COMMENT,obj.getString("text"));
                map.put(KEY_LIKE, obj.getString("like"));
                map.put(KEY_COMMENTTIME, obj.getString("posted"));
                map.put(KEY_TIMELINE,obj.getString("timeline"));
                //map.put(KEY_USERIMAGE,obj.getString("courselink"));
                commentList.add(map);
            }
            mCommentAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
