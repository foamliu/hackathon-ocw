package org.hackathon_ocw.androidclient.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.golshadi.majid.core.DownloadManagerPro;
import com.golshadi.majid.core.enums.TaskStates;
import com.golshadi.majid.report.ReportStructure;

import org.hackathon_ocw.androidclient.domain.Course;
import org.hackathon_ocw.androidclient.util.ImageLoader;
import org.hackathon_ocw.androidclient.util.NetworkThread;
import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.util.StorageUtils;
import org.hackathon_ocw.androidclient.domain.UserProfile;
import org.hackathon_ocw.androidclient.activity.DetailActivity;
import org.hackathon_ocw.androidclient.util.Constants;
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
    private DownloadManagerPro downloadManager;
    private ArrayList<HashMap<String, String>> dataList;
    private final ImageLoader imageLoader;

    public DownloadListAdapter(Activity activity, DownloadManagerPro dm) {
        this.appContext = activity.getApplicationContext();
        this.downloadManager = dm;
        this.dataList = new ArrayList<>();
        this.imageLoader = new ImageLoader(activity.getApplicationContext());

        init();
    }

    private void init() {
        StorageUtils.clean();
        this.loadData();
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

        View vi;
        if (convertView != null)
            vi = convertView;
        else {
            vi = LayoutInflater.from(appContext).inflate(R.layout.download_list_item, parent, false);
        }

        HashMap<String, String> item = dataList.get(position);
        final String strItemId = item.get(Constants.KEY_ID);
        final long itemId = Long.parseLong(strItemId);
        String strTemp = item.get(Constants.KEY_TITLE);
        if (strTemp.length() > 20)
            strTemp = strTemp.substring(0, 20) + "..";
        final String strTitle = strTemp;
        final String description = item.get(Constants.KEY_DESCRIPTION);
        final String thumbUrl = item.get(Constants.KEY_THUMB_URL);
        final String videoUrl = StorageUtils.FILE_ROOT + strItemId + ".mp4";
        final String webUrl = item.get(Constants.KEY_WEBURL);
        final String strTaskId = item.get("taskId");
        final int taskId = Integer.parseInt(strTaskId);
        String strPercent = item.get("percent");
        double percent = Double.parseDouble(strPercent);
        int state = Integer.parseInt(item.get("state"));
        long lFileSize = Long.parseLong(item.get("fileSize"));

        ImageView thumbImage = (ImageView) vi.findViewById(R.id.pic_link);
        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView progress = (TextView) vi.findViewById(R.id.progress);
        TextView fileSize = (TextView) vi.findViewById(R.id.fileSize);

        imageLoader.DisplayImage(thumbUrl, thumbImage);
        title.setText(strTitle);
        progress.setText(String.format("%.2f", percent) + "%");
        fileSize.setText(String.format("%.1f", 1.0 * lFileSize / 1024 / 1024) + "M");

        ImageButton playButton = (ImageButton) vi.findViewById(R.id.btn_play);
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
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(appContext, DetailActivity.class);
                appContext.startActivity(intent);

                Course course = new Course(itemId,strTitle,description,thumbUrl,videoUrl,webUrl);
                //Send post to server
                Runnable networkTask = new NetworkThread(UserProfile.getInstance().getUserId(), course, 3);
                new Thread(networkTask).start();
            }
        });
        if (state == TaskStates.END) {
            playButton.setEnabled(true);
            setUnlocked(playButton);
        } else {
            setLocked(playButton);
            playButton.setEnabled(false);
        }

        ImageButton deleteButton = (ImageButton) vi.findViewById(R.id.btn_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    downloadManager.pauseDownload(taskId);
                } catch (Exception ex) {
                }
                try {
                    downloadManager.delete(taskId, true);
                } catch (Exception ex) {
                }
                StorageUtils.delete(itemId);
                delete(itemId);
                notifyDataSetChanged();
            }
        });

        return vi;
    }

    public static void setLocked(ImageView v) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
        v.setAlpha(128);   // 128 = 0.5
    }

    public static void setUnlocked(ImageView v) {
        v.setColorFilter(null);
        v.setAlpha(255);
    }

    public void loadData() {
        dataList.clear();

        try {
            String fileName = "download.json";
            File file = new File(StorageUtils.FILE_ROOT, fileName);

            if (file.exists()) {
                InputStream inputStream = new FileInputStream(file);

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

    public void writeToDisk() {
        JSONArray list = new JSONArray();
        try {
            int index = 0;
            for (HashMap<String, String> item : dataList) {
                JSONObject jsonObject = new JSONObject();
                for (Object o : item.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    jsonObject.put((String) pair.getKey(), pair.getValue());
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

        } catch (Exception e) {
            Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addItem(HashMap<String, String> item) {
        if (!contains(item)) {
            dataList.add(0, item);
            writeToDisk();
        }
    }

    private void delete(long itemId) {
        for (int i = 0; i < dataList.size(); i++) {
            HashMap<String, String> item = dataList.get(i);
            if (String.valueOf(itemId).equals(item.get(Constants.KEY_ID))) {
                dataList.remove(i);
                writeToDisk();
                return;
            }
        }
    }

    public boolean contains(HashMap<String, String> item) {
        for (HashMap<String, String> i : dataList) {
            if (item.get(Constants.KEY_ID).equals(i.get(Constants.KEY_ID)))
                return true;
        }
        return false;
    }

    private int getPos(long taskId) {
        int pos = -1;
        HashMap<String, String> item = null;
        for (int i = 0; i < dataList.size(); i++) {
            item = dataList.get(i);
            String strTaskId = item.get("taskId");
            int thisTaskId = Integer.parseInt(strTaskId);
            if (thisTaskId == taskId) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public void OnDownloadStarted(long taskId) {
        int pos = getPos(taskId);
        HashMap<String, String> item = dataList.get(pos);

        if (pos != -1 && item != null) {
            ReportStructure report = downloadManager.singleDownloadStatus((int)taskId);
            JSONObject result = report.toJsonObject();
            try {
                long fileSize = (long) result.get("fileSize");
                item.put("fileSize", String.valueOf(fileSize));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDownloadProcess(long taskId, double percent) {
        int pos = getPos(taskId);
        HashMap<String, String> item = dataList.get(pos);

        if (pos != -1 && item != null) {
            item.put("percent", String.valueOf(percent));
        }
    }

    public void OnDownloadCompleted(long taskId) {
        int pos = getPos(taskId);
        HashMap<String, String> item = dataList.get(pos);

        if (pos != -1 && item != null) {
            item.put("percent", String.valueOf(100.00));
            item.put("state", String.valueOf(TaskStates.END));
            writeToDisk();
        }
    }

    public void onResume() {
        for (HashMap<String, String> item: dataList) {
            String strTaskId = item.get("taskId");
            int taskId = Integer.parseInt(strTaskId);
            ReportStructure report = downloadManager.singleDownloadStatus(taskId);
            JSONObject result = report.toJsonObject();
            try {
                int state = Integer.parseInt(String.valueOf(result.get("state")));
                boolean resumable = Boolean.parseBoolean(String.valueOf(result.get("resumable")));
                if (state != TaskStates.END && resumable) {
                    downloadManager.startDownload(taskId);
                }
            } catch (JSONException|IOException e) {
                e.printStackTrace();
            }

        }
    }

}
