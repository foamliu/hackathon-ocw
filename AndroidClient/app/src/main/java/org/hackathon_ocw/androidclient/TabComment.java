package org.hackathon_ocw.androidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

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

    static final String KEY_AUTHORID = "author_id";
    static final String KEY_USERNAME = "userName";
    static final String KEY_COMMENT = "comment";
    static final String KEY_COMMENT_ID = "commentId";
    static final String KEY_COMMENTTIME = "commentTime";
    static final String KEY_LIKE = "like";
    static final String KEY_USERIMAGE = "headimgurl";
    static final String KEY_TIMELINE = "timeline";
    static final String KEY_HEADIMGURL = "headimgurl";
    static final String getCommentUrl = "http://jieko.cc/item/";
    int iterator;

    public ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.tab_comment_detail, container, false);

        Bundle b = getActivity().getIntent().getExtras();
        String courseid = b.getString("id");

        final Download_data download_data = new Download_data((Download_data.download_complete) this);
        download_data.download_data_from_link(getCommentUrl + courseid + "/Comments");

        mCommentView = (ListView) inflatedView.findViewById(R.id.commentList);
        mCommentAdapter = new CommentAdapter(getActivity(), commentList);
        mCommentView.setAdapter(mCommentAdapter);

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
                map.put(KEY_AUTHORID, String.valueOf(obj.getInt("author_id")));
                map.put(KEY_USERNAME,obj.getString("author_name"));
                map.put(KEY_COMMENT,obj.getString("text"));
                map.put(KEY_LIKE, obj.getString("like"));
                map.put(KEY_COMMENTTIME, obj.getString("posted"));
                map.put(KEY_TIMELINE, obj.getString("timeline"));
                commentList.add(map);
            }
            mCommentAdapter.notifyDataSetChanged();
            GetHeadImage(commentList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void GetHeadImage(final ArrayList<HashMap<String, String>> list) {
        //Send Get request
        for(iterator = 0; iterator < list.size(); iterator++)
        {
            final String author_id = list.get(iterator).get("author_id");
            String url = "http://jieko.cc/user/" + author_id;

            //Send Request here
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.getString(0));
                                list.get(iterator-1).put(KEY_HEADIMGURL, (String) jsonObject.get("headimgurl"));
                                mCommentAdapter.notifyDataSetChanged();
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
            requestQueue.add(jsonArrayRequest);
        }
    }

}
