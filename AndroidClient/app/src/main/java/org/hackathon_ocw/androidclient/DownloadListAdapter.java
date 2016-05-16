package org.hackathon_ocw.androidclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Foam on 2016/5/15.
 */
public class DownloadListAdapter extends BaseAdapter {

    private final static String TAG = "DownloadListAdapter";
    private final Context appContext;
    private ArrayList<HashMap<String, String>> dataList;

    public DownloadListAdapter(Activity activity) {
        this.appContext = activity.getApplicationContext();
        this.dataList = new ArrayList<>();

        init();
    }

    private void init() {
        this.loadData();
        //this.repairData();
        //this.updateData();
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = null;
        if (convertView != null)
            vi = convertView;
        else {
            vi = LayoutInflater.from(appContext).inflate(R.layout.download_list_item, parent, false);
        }

        HashMap<String, String> item = dataList.get(position);
        final String strItemId = item.get(Constants.KEY_ID);
        final String strTitle = item.get(Constants.KEY_TITLE);
        final String description = item.get(Constants.KEY_DESCRIPTION);
        final String thumbUrl = item.get(Constants.KEY_THUMB_URL);
        final String videoUrl = StorageUtils.FILE_ROOT + strItemId + ".mp4";
        String strPercent = item.get("percent");
        double percent = Double.parseDouble(strPercent);

        TextView title = (TextView) vi.findViewById(R.id.title);
        ProgressBar progress = (ProgressBar) vi.findViewById(R.id.progress_bar);

        title.setText(strTitle);
        progress.setProgress((int) Math.round(percent));

        Button playButton = (Button) vi.findViewById(R.id.btn_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("id", strItemId);
                intent.putExtra("title", strTitle);
                intent.putExtra("description", description);
                intent.putExtra("videoUrl", videoUrl);
                intent.putExtra("videoImg", thumbUrl);
                intent.putExtra("userid", UserProfile.getInstance().getUserId());
                if (UserProfile.getInstance().getNickname() != null) {
                    intent.putExtra("nickname", UserProfile.getInstance().getNickname());
                    intent.putExtra("headimgurl", UserProfile.getInstance().getHeadimgurl());
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(appContext, DetailActivity.class);
                appContext.startActivity(intent);
            }
        });

        Button deleteButton = (Button) vi.findViewById(R.id.btn_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadListAdapter.this.delete(Long.parseLong(strItemId));
            }
        });

        return vi;
    }

    public void loadData() {
        dataList.clear();

        try {
            String fileName = "download.json";
            File file = new File(StorageUtils.FILE_ROOT, fileName);

            if (file.exists()) {
                InputStream inputStream = new FileInputStream(file);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }
                    inputStream.close();
                    String str = stringBuilder.toString();

                    JSONArray list = new JSONArray(str);
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject jObject = list.getJSONObject(i);
                        Iterator<?> keys = jObject.keys();

                        HashMap<String, String> item = new HashMap<>();

                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            if (jObject.get(key) instanceof String) {
                                item.put(key, (String) jObject.get(key));
                            }
                        }
                        dataList.add(item);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(appContext, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Toast.makeText(appContext, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Can not read file: " + e.toString());
        } catch (JSONException e) {
            Toast.makeText(appContext, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Can not parse json string: " + e.toString());
        }
    }

    private void repairData() {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            HashMap<String, String> item = dataList.get(i);
            String courseId = item.get("id");
            File file = new File(StorageUtils.FILE_ROOT, courseId + ".mp4");
            if (!file.exists()) {
                dataList.remove(i);
            }
        }
    }

    public void updateData() {
        JSONArray list = new JSONArray();
        try {
            int index = 0;
            for (HashMap<String, String> item : dataList) {
                JSONObject jsonObject = new JSONObject();
                Iterator it = item.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    jsonObject.put((String) pair.getKey(), (String) pair.getValue());
                }
                list.put(index, jsonObject);
                index += 1;
            }

            //Write to local file
            String fileName = "download.json";
            File file = new File(StorageUtils.FILE_ROOT, fileName);
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(list.toString());
            bw.close();

        } catch (JSONException e) {
            Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addItem(HashMap<String, String> item) {
        dataList.add(item);
    }

    public void updateProgress(long taskId, double percent) {
        for (HashMap<String, String> item : dataList) {
            if (String.valueOf(taskId).equals(item.get("taskId"))) {
                item.put("percent", String.valueOf(percent));
            }
        }
    }

    private void delete(long itemId) {
        for (int i = 0; i < dataList.size(); i++) {
            HashMap<String, String> item = dataList.get(i);
            if (String.valueOf(itemId).equals(item.get(Constants.KEY_ID))) {
                dataList.remove(i);
                return;
            }
        }
    }
}
