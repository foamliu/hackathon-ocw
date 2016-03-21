package org.hackathon_ocw.androidclient.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.hackathon_ocw.androidclient.Constants;

/**
 * Created by dianyang on 2016/3/21.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static String TAG = "WXEntryActivity";
    private IWXAPI api;
    public static BaseResp mResp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onReq(BaseReq arg0) {
        Log.i(TAG, "onReq");
        finish();
    }

    /**
     *认证后会回调该方法
     */
    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode){
            case BaseResp.ErrCode.ERR_OK:
                mResp = resp;
                //Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                //Toast.makeText(this, "User Cancel", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //Toast.makeText(this, "Auth Denied", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                mResp = resp;
                //Toast.makeText(this, "User Return", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        //String code = ((SendAuth.Resp) resp).code;
        //获取code后需要去获取access_token
    }



}
