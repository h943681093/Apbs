package com.hmy.apbs.apbs;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;

/**
 * Created by Administrator on 2015/10/2.
 */
public class MainApplication extends Application {

    public LocationClient mLocationClient;
    public GeofenceClient mGeofenceClient;
    public MyLocationListener mMyLocationListener;

    public TextView mLocationResult,logMsg;
    public TextView trigger,exit;
    public Vibrator mVibrator;

    public static int ISin = 0;
    public String username = "请登录";
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        mGeofenceClient = new GeofenceClient(getApplicationContext());


        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
    }
    /**
     *
     * 是否登陆 getISin getISin
     *
     * @param
     */
    public int getISin() {
        return ISin;
    }
    public void setISin(int ISin) {
        this.ISin = ISin;
        Log.e("全局变量", "" + ISin);
    }
    /**
     * 获得用户名称
     *
     * @return
     */
    public String getusername() {
        return username;
    }
    public void setusername(String username) {
        this.username = username;
        Log.e("全局变量username", username);
    }
    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());
            logMsg(sb.toString());
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }
        /**
         * 显示请求字符串
         * @param str
         */
        public void logMsg(String str) {
            try {
                if (mLocationResult != null)
                    mLocationResult.setText(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
