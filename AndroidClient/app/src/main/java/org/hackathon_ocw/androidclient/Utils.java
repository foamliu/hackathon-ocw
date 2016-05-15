package org.hackathon_ocw.androidclient;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * Created by dianyang on 2016/2/29.
 */
public class Utils {

    //Append Wechat login function
    public static String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    //获取用户个人信息
    public static String GetUserInfo="https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    public static String getUserInfo(String access_token,String openid){
        String result;
        GetUserInfo = GetUserInfo.replace("ACCESS_TOKEN",
                urlEnodeUTF8(access_token));
        GetUserInfo = GetUserInfo.replace("OPENID",
                urlEnodeUTF8(openid));
        result = GetUserInfo;
        return result;
    }

    public static String getCodeRequest(String code) {
        String result;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEnodeUTF8(Constants.APP_ID));
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEnodeUTF8(Constants.APP_SECRET));
        GetCodeRequest = GetCodeRequest.replace("CODE",urlEnodeUTF8(code));
        result = GetCodeRequest;
        return result;
    }

    public static String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
                is.close();
                os.close();
            }
        }
        catch(Exception ignored){}
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        bmp.recycle();

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return result;
    }
}
