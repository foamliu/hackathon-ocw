package org.hackathon_ocw.androidclient.util;

import android.util.Log;

import org.hackathon_ocw.androidclient.domain.Item;
import org.hackathon_ocw.androidclient.domain.HistoryEntry;
import org.hackathon_ocw.androidclient.domain.UserProfile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by dianyang on 2016/3/7.
 */
public class NetworkThread implements Runnable{
    private final static String TAG = "NetworkThread";

    static final String postUrl = "http://api.jieko.cc/user/";

    private float rating = 5;
    private final Item item;

    private final String userid;

    public NetworkThread(String userid, Item item, float rating)
    {
        this.userid = userid;
        this.item = item;
        this.rating = rating;
    }

    @Override
    public void run() {
        try {
            setLocal(item);
            SendPostRequest(Long.valueOf(userid), item.getItemid(), rating);
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setLocal(Item item) {
        HistoryEntry he = new HistoryEntry();
        he.item = item;
        String timeString = Constants.DateFormat.format(new Date());
        he.watchedTime = timeString;
        UserProfile.getInstance().addHistoryEntry(he);
    }

    public void SendPostRequest(Long userId, long itemId, float rating) throws Exception
    {
        String params = "{\"user_id\":" + Long.toString(userId) + ",\"item_id\":" + itemId + ",\"pref\":" + Double.toString(rating) + "}";
        URL url = new URL(postUrl + Long.toString(userId) + "/Preferences");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        byte[] data = params.getBytes();
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();

        int responseCode = conn.getResponseCode(); // getting the response code
        final StringBuilder output = new StringBuilder("Request URL " + url);
        output.append(System.getProperty("line.separator")).append("Request Parameters ").append(postUrl);
        output.append(System.getProperty("line.separator")).append("Response Code ").append(responseCode);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder responseOutput = new StringBuilder();
        System.out.println(br);
        while((line = br.readLine()) != null ) {
            responseOutput.append(line);
        }
        br.close();

        output.append(System.getProperty("line.separator")).append("Response ").append(System.getProperty("line.separator")).append(System.getProperty("line.separator")).append(responseOutput.toString());
        Log.i(TAG, output.toString());
    }

}
