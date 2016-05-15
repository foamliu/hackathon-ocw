package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Foam on 2016/5/15.
 */
public class DownloadListAdapter extends BaseAdapter {

    private final Context appContext;

    public DownloadListAdapter(Activity activity)
    {
        this.appContext = activity.getApplicationContext();
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
