package org.hackathon_ocw.androidclient;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by dianyang on 2016/3/7.
 */
/*
public class GetUserIdFromIP {

    public long ipToLong(String ipString){
        long result = 0;
        java.util.StringTokenizer token = new java.util.StringTokenizer(ipString,".");
        result += Long.parseLong(token.nextToken())<<24;
        result += Long.parseLong(token.nextToken())<<16;
        result += Long.parseLong(token.nextToken())<<8;
        result += Long.parseLong(token.nextToken());
        return result;
    }

    public Long getUserId()
    {
        String ipAddress = getLocalIPAddress();
        //Hash ipAddress to a long
        long ipAddrLong = ipToLong(ipAddress) % 100 + 1;
        return ipAddrLong;
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
}
*/
