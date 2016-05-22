package org.hackathon_ocw.androidclient;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dianyang on 2016/3/22.
 */
public class UserProfile {

    private static UserProfile instance = null;
    private final static String TAG = "UserProfile";

    private String userId;
    private String deviceid;
    private String nickname;
    private int sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;

    private Context appContext;

    public static void init(Context context) {
        instance = new UserProfile();
        instance.appContext = context;
        instance.getUserProfileFromFile();
    }

    public static UserProfile getInstance() {
        if (instance == null) {
            instance = new UserProfile();
        }
        return instance;
    }

    public String getOpenId() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    private String openid;

    public String getDeviceId() {
        return deviceid;
    }

    public void setDeviceId(String deviceId) {
        this.deviceid = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public void clearProfile() {
        this.nickname = "";
        this.sex = -1;
        this.province = "";
        this.country = "";
        this.headimgurl = "";
    }

    public void getUserProfileFromFile() {
        String str;
        //Read from local user profile
        try {
            String fileName = StorageUtils.FILE_ROOT + "userProfile.json";
            File file = new File(fileName);
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }
            inputStream.close();
            str = stringBuilder.toString();
            if (!str.contains("userid")) {
                retrieveUserId();
                return;
            }
            //Parse
            try {
                JSONObject jsonObject = new JSONObject(str);
                this.userId = jsonObject.getString("userid");
                this.deviceid = jsonObject.getString("deviceid");
                this.openid = jsonObject.getString("openid");
                this.nickname = jsonObject.getString("nickname");
                this.sex = jsonObject.getInt("sex");
                this.city = jsonObject.getString("city");
                this.province = jsonObject.getString("province");
                this.country = jsonObject.getString("country");
                this.headimgurl = jsonObject.getString("headimgurl");
            } catch (Exception e) {
                Log.w("UserProfile", e.getMessage());
            }

        } catch (FileNotFoundException e) {
            retrieveUserId();
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
    }

    public void retrieveUserId() {
        String url = "http://jieko.cc/user";
        String android_id = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        UserProfile.getInstance().setDeviceId(android_id);
        //Send POST request
        RequestQueue requestQueue = Volley.newRequestQueue(appContext);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceid", android_id);
        } catch (Exception e) {
            Log.e("Json Error", e.toString());
        }
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            UserProfile.getInstance().setUserId(String.valueOf((Long) response.getLong("userid")));
                            setUserProfile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        requestQueue.add(jsonRequest);
    }

    public void setUserProfile() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid", this.userId);
            jsonObject.put("deviceid", this.deviceid);
        } catch (Exception e) {
            Log.e("Json Error", e.toString());
        }

        String fileName = StorageUtils.FILE_ROOT + "userProfile.json";
        File file = new File(fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateLocalAndRemote() {
        //Update local user profile
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid", this.userId);
            jsonObject.put("openid", this.openid);
            jsonObject.put("nickname", this.nickname);
            jsonObject.put("sex", this.sex);
            jsonObject.put("city", this.city);
            jsonObject.put("province", this.province);
            jsonObject.put("country", this.country);
            jsonObject.put("headimgurl", this.headimgurl);
            if (this.deviceid == null) {
                this.deviceid = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            jsonObject.put("deviceid", this.deviceid);
        } catch (Exception e) {
            Log.e("Json Error", e.toString());
        }


        //Write to local file
        String fileName = "userProfile.json";
        File userProfileFile = new File(appContext.getFilesDir(), fileName);
        try {
            FileWriter fw = new FileWriter(userProfileFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Send PATCH to server
        RequestQueue requestQueue = Volley.newRequestQueue(appContext);
        jsonObject.remove("userid");
        jsonObject.remove("sex");
        try {
            jsonObject.put("_id", Integer.valueOf(this.userId));
            jsonObject.put("sex", Integer.valueOf(this.sex));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String httpurl = "http://jieko.cc/user/" + this.userId;

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, httpurl, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                return headers;
            }
        };
        requestQueue.add(jsonRequest);
    }

    public void WXLogin() {
        IWXAPI WXapi = WXAPIFactory.createWXAPI(appContext, Constants.APP_ID, true);
        if (!WXapi.isWXAppInstalled()) {
            Toast.makeText(appContext, "请先安装微信", Toast.LENGTH_SHORT).show();
            return;
        }
        WXapi.registerApp(Constants.APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "hackathon_ocw";
        boolean res = WXapi.sendReq(req);
    }

    public void WXLogout() {
        //clear userprofile.json
        UserProfile.getInstance().clearProfile();

        //MainActivity.Self.updateNaviViewWithUserProfile();

        //Update local user profile
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid", this.userId);
            if (UserProfile.getInstance().getDeviceId() == null) {
                UserProfile.getInstance().setDeviceId(Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID));
            }
            jsonObject.put("deviceid", UserProfile.getInstance().getDeviceId());
        } catch (Exception e) {
            Log.e("Json Error", e.toString());
        }

        //Write to local file
        String fileName = "userProfile.json";
        File userProfileFile = new File(appContext.getFilesDir(), fileName);
        try {
            FileWriter fw = new FileWriter(userProfileFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(jsonObject.toString());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void WXGetUserInfo(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(appContext);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            UserProfile.getInstance().setOpenid((String) response.get("openid"));
                            UserProfile.getInstance().setNickname((String) response.get("nickname"));
                            UserProfile.getInstance().setSex((Integer) response.get("sex"));
                            UserProfile.getInstance().setCity((String) response.get("city"));
                            UserProfile.getInstance().setProvince((String) response.get("province"));
                            UserProfile.getInstance().setCountry((String) response.get("country"));
                            UserProfile.getInstance().setHeadimgurl((String) response.get("headimgurl"));
                            //MainActivity.Self.updateUserProfile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(
                    NetworkResponse arg0) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(
                            arg0.data, "UTF-8"));
                    return Response.success(jsonObject,
                            HttpHeaderParser.parseCacheHeaders(arg0));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception je) {
                    return Response.error(new ParseError(je));
                }
            }

        };

        requestQueue.add(jsonRequest);
    }
}
