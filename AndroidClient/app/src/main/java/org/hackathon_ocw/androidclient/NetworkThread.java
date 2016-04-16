package org.hackathon_ocw.androidclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dianyang on 2016/3/7.
 */
public class NetworkThread implements Runnable{

    static final String postUrl = "http://api.jieko.cc/user/";

    private float rating = 5;

    private String courseId;

    private String userid;

    public NetworkThread(String userid, String courseId, float rating)
    {
        this.userid = userid;
        this.courseId = courseId;
        this.rating = rating;
    }

    @Override
    public void run() {

        //long ipAddrLong = new GetUserIdFromIP().getUserId();
        //Toast.makeText(getApplicationContext(), ipAddress, Toast.LENGTH_SHORT).show();
        try {
            SendPostRequest(Long.valueOf(userid), courseId, rating);
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void SendPostRequest(Long userId, String itemId, float rating) throws Exception
    {

        String encoding="UTF-8";
        String params = "{\"user_id\":" + Long.toString(userId) + ",\"item_id\":" + itemId + ",\"pref\":" + Double.toString(rating) + "}";
        URL url = new URL(postUrl + Long.toString(userId) + "/Preferences");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        byte[] data = params.toString().getBytes();
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();

        int responseCode = conn.getResponseCode(); // getting the response code
        final StringBuilder output = new StringBuilder("Request URL " + url);
        output.append(System.getProperty("line.separator") + "Request Parameters " + postUrl);
        output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = "";
        StringBuilder responseOutput = new StringBuilder();
        System.out.println(br);
        while((line = br.readLine()) != null ) {
            responseOutput.append(line);
        }
        br.close();

        output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());
    }

}
