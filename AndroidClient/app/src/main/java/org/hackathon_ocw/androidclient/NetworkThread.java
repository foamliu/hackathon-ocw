package org.hackathon_ocw.androidclient;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Created by dianyang on 2016/3/7.
 */
public class NetworkThread implements Runnable{

    static final String postUrl = "http://jieko.cc/Preferences";

    static final float rating = 5;

    public String courseId;

    public NetworkThread(String courseId)
    {
        this.courseId = courseId;
    }

    @Override
    public void run() {
        // 在这里进行 http request.网络请求相关操作
        String ipAddress = getLocalIPAddress();
        //Hash ipAddress to a long
        long ipAddrLong = ipToLong(ipAddress) % 100 + 1;

        //Toast.makeText(getApplicationContext(), ipAddress, Toast.LENGTH_SHORT).show();
        try {
            SendPostRequest(ipAddrLong, courseId, rating);
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public long ipToLong(String ipString){
        long result = 0;
        java.util.StringTokenizer token = new java.util.StringTokenizer(ipString,".");
        result += Long.parseLong(token.nextToken())<<24;
        result += Long.parseLong(token.nextToken())<<16;
        result += Long.parseLong(token.nextToken())<<8;
        result += Long.parseLong(token.nextToken());
        return result;
    }

    public String getLocalIPAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        // return inetAddress.getAddress().toString();
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("BaseScanTvDeviceClient", "Fetch ip false =" + ex.toString());
        }
        return null;
    }

    public void SendPostRequest(Long userId, String itemId, float rating) throws Exception
    {

        String encoding="UTF-8";
        String params = "{\"user_id\":" + Long.toString(userId) + ",\"item_id\":" + itemId + ",\"pref\":" + Double.toString(rating) + "}";
        URL url = new URL(postUrl);
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
