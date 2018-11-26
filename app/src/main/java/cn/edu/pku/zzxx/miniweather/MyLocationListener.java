package cn.edu.pku.zzxx.miniweather;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zzxx.app.MyApplication;
import cn.edu.pku.zzxx.bean.City;

import static java.text.DateFormatSymbols.getInstance;

/**
 * Created by Administror on 2018/11/18.
 */

public class MyLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
        String addr = location.getAddrStr();    //获取详细地址信息
        String country = location.getCountry();    //获取国家
        String province = location.getProvince();    //获取省份
        String city = location.getCity();    //获取城市
        String district = location.getDistrict();    //获取区县
        String street = location.getStreet();    //获取街道信息
        Log.d("TAG",location.getCity());
        Const.city=district.substring(0,district.length()-1);


        //if(location.getCity()=="北京市")
            //Const.city=district.substring(0,district.length()-1);
       // else
           // Const.city=city.substring(0,city.length()-1);
        //Log.d("TAG",Const.city);
    }
}
