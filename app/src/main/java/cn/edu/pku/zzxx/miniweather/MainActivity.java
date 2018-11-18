package cn.edu.pku.zzxx.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zzxx.app.MyApplication;
import cn.edu.pku.zzxx.bean.City;
import cn.edu.pku.zzxx.bean.TodayWeather;
import cn.edu.pku.zzxx.util.NetUtil;

/**
 * Created by Administror on 2018/10/23.
 */

public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {


    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();




    private ViewPagerAdapter vpAdapter1;
    private ViewPager vp1;
    private List<View> views1;
    private ImageView[]dots1;
    private int[]ids1={R.id.iv4,R.id.iv5};
    void initDots(){
        dots1 = new ImageView[views1.size()];
        for(int i = 0;i<views1.size();i++){
            dots1[i]=(ImageView)findViewById(ids1[i]);
        }
    }
    private void initViews(){
        LayoutInflater inflater1 = LayoutInflater.from(this);
        views1= new ArrayList<View>();
        views1.add(inflater1.inflate(R.layout.page4,null));
        views1.add(inflater1.inflate(R.layout.page5,null));
        vpAdapter1 = new ViewPagerAdapter(views1,this);
        vp1=(ViewPager)findViewById(R.id.viewpager2);
        vp1.setAdapter(vpAdapter1);
        vp1.setOnPageChangeListener(this);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int a = 0;a<ids1.length;a++){
            if (a==position)
                dots1[a].setImageResource(R.drawable.page_indicator_focused);
            else
                dots1[a].setImageResource(R.drawable.page_indicator_unfocused);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }








    private static final int UPDATE_TODAY_WEATHER = 1;

    private ProgressBar progressBar;



    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private TextView w11,w12,w13,w14,w21,w22,w23,w24,w31,w32,w33,w34,w41,w42,w43,w44,w45,w46;
    private ImageView weatherImg, pmImg;
    private ImageView location;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);



















        progressBar=(ProgressBar)findViewById(R.id.title_update_progress) ;


        location = (ImageView)findViewById(R.id.title_location);
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        LocationClientOption option = new LocationClientOption();

        option.setIsNeedAddress(true);
         //可选，是否需要地址信息，默认为不需要，即参数为false
          //如果开发者需要获得当前点的地址信息，此处必须为true

        mLocationClient.setLocOption(option);
          //mLocationClient为第二步初始化过的LocationClient对象
         //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
          //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明






        mUpdateBtn=(ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        //以下判断是用来判断网络状态
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this,"网络OK！", Toast.LENGTH_LONG).show();
        }else
        {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }
        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initView();


        initViews();
        initDots();
        location.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mLocationClient.start();
                MyApplication myapp = (MyApplication)getApplication();
                List<City> listcity = myapp.getCityList();//存放数据库中城市信息
                //该循环从城市信息中获取城市名称和相应城市代码
                for(City c:listcity){
                    if(c.getCity().equals(Const.city)){
                        String citynumber = c.getNumber();
                        queryWeatherCode(citynumber);

                    }
                }
            }
        });
    }


    //以下代码是用来响应点击事件，
    public void onClick(View view){

        //若点击的是城市切换按钮则触发新的活动，打开新的页面
        if(view.getId()==R.id.title_city_manager){
            Intent i =new Intent(this,SelectCity.class);
            startActivityForResult(i,1);
        }
        //若点击的是更新按钮，则根据城市代码获取响应城市的最新的天气情况
        if(view.getId()==R.id.title_update_btn) {
            mUpdateBtn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
           //sharesPreferences 以键值对的形式存储数据，这里存储的是默认的城市 即北京的城市代码
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);
            //根据城市代码获取天气时，首先判断网络状态是否良好
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                progressBar.setVisibility(View.GONE);
                mUpdateBtn.setVisibility(View.VISIBLE);
                queryWeatherCode(cityCode);//根据城市代码访问网络，获取天气信息，并更新
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }

    }
    //当切换城市获取天气信息时，根据解析得到的城市代码，访问网络，获取天气情况
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);//根据解析得到的城市代码，获取天气信息，并更新
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }



//这是初始化函数，当打开软件时，用该函数初始化界面
    void initView(){
        progressBar.setVisibility(View.GONE);
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality
        );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature
        );
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");











    }
//将网络获取的天气信息进行解析
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try {
            //获得xmlpullparser对象
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
           // 把文档设置给Parser
            xmlPullParser.setInput(new StringReader(xmldata));
            //开始解析
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                 // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                    } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                    } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                    } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                    } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                    } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                    }else if(xmlPullParser.getName().equals("date")&&dateCount==1){
                                eventType = xmlPullParser.next();
                                todayWeather.setNextdate1(xmlPullParser.getText());
                                dateCount++;
                    }else if(xmlPullParser.getName().equals("high")&&highCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                    }else if(xmlPullParser.getName().equals("low")&&lowCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                    }else if(xmlPullParser.getName().equals("type")&&typeCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                typeCount++;
                    }else if(xmlPullParser.getName().equals("fengli")&&typeCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                fengliCount++;
                    }else if(xmlPullParser.getName().equals("date")&&dateCount==2){
                                eventType = xmlPullParser.next();
                                todayWeather.setNextdate2(xmlPullParser.getText());
                                dateCount++;
                    }else if(xmlPullParser.getName().equals("high")&&highCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                    }else if(xmlPullParser.getName().equals("low")&&lowCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                    }else if(xmlPullParser.getName().equals("type")&&typeCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                typeCount++;
                    }else if(xmlPullParser.getName().equals("fengli")&&typeCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                fengliCount++;
                    }else if(xmlPullParser.getName().equals("date")&&dateCount==3){
                                eventType = xmlPullParser.next();
                                todayWeather.setNextdate3(xmlPullParser.getText());
                                dateCount++;
                    }else if(xmlPullParser.getName().equals("high")&&highCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                    }else if(xmlPullParser.getName().equals("low")&&lowCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                    }else if(xmlPullParser.getName().equals("type")&&typeCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                typeCount++;
                    }else if(xmlPullParser.getName().equals("fengli")&&typeCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                fengliCount++;
                    }else if(xmlPullParser.getName().equals("date")&&dateCount==4){
                                eventType = xmlPullParser.next();
                                todayWeather.setNextdate4(xmlPullParser.getText());
                                dateCount++;
                    }else if(xmlPullParser.getName().equals("high")&&highCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                    }else if(xmlPullParser.getName().equals("low")&&lowCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                    }else if(xmlPullParser.getName().equals("type")&&typeCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setType4(xmlPullParser.getText());
                                typeCount++;
                    }else if(xmlPullParser.getName().equals("fengli")&&typeCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli4(xmlPullParser.getText());
                                fengliCount++;
                    }
















                }
                break;
                 // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    break;
            }
              // 进入下一个元素并触发相应事件
            eventType = xmlPullParser.next();
        }
    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
return todayWeather;
}
//此函数根据解析得到的数据更新今日天气
    void updateTodayWeather(TodayWeather todayWeather){

            w11=(TextView)findViewById(R.id.w11);
            w12=(TextView)views1.get(0).findViewById(R.id.w12);
            w13=(TextView)views1.get(0).findViewById(R.id.w13);
            w14=(TextView)views1.get(0).findViewById(R.id.w14);
            w21=(TextView)views1.get(0).findViewById(R.id.w21);
            w22=(TextView)views1.get(0).findViewById(R.id.w22);
            w23=(TextView)views1.get(0).findViewById(R.id.w23);
            w24=(TextView)views1.get(0).findViewById(R.id.w24);
            w31=(TextView)views1.get(1).findViewById(R.id.w31);
            w32=(TextView)views1.get(1).findViewById(R.id.w32);
            w33=(TextView)views1.get(1).findViewById(R.id.w33);
            w34=(TextView)views1.get(1).findViewById(R.id.w34);
            w41=(TextView)views1.get(1).findViewById(R.id.w41);
            w42=(TextView)views1.get(1).findViewById(R.id.w42);
            w43=(TextView)views1.get(1).findViewById(R.id.w43);
            w44=(TextView)views1.get(1).findViewById(R.id.w44);
            w11.setText(todayWeather.getNextdate1());
            w21.setText(todayWeather.getNextdate2());
            w31.setText(todayWeather.getNextdate3());
            w41.setText(todayWeather.getNextdate4());
            w12.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
            w22.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
            w32.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
            w42.setText(todayWeather.getHigh4()+"~"+todayWeather.getLow3());
            w13.setText(todayWeather.getType1());
            w23.setText(todayWeather.getType2());
            w33.setText(todayWeather.getType3());
            w43.setText(todayWeather.getType4());
            w14.setText("风力"+todayWeather.getFengli1());
            w24.setText("风力"+todayWeather.getFengli2());
            w34.setText("风力"+todayWeather.getFengli3());
            w44.setText("风力"+todayWeather.getFengli4());





        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());

        if(todayWeather.getPm25()!=null)
        pmDataTv.setText(todayWeather.getPm25());
        if(todayWeather.getQuality()!=null)
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        //根据天气情况，更新天气质量所对应的图片
        if(todayWeather.getQuality()!=null) {
            switch (todayWeather.getQuality()) {
                case "优":
                    pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
                    break;
                case "良":
                    pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
                    break;
                case "轻度污染":
                    pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
                    break;
                case "中度污染":
                    pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
                    break;
                case "重度污染":
                    pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
                    break;
                default:
                    pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
                    break;

            }
        }
        //根据解析得到的天气信息，更新天气类型
        if(todayWeather.getType()!=null){
        switch(todayWeather.getType()){
            case "暴雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case"暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case"大雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case"雷阵雨冰雹":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case"特大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case"小雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case"阵雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case"中雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            default:
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
        }
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }}





//根据城市代码，从天气网站获取天气信息
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        //建立线程，该线程是用来实现访问天气网站，获取天气信息的功能，减少主线程负担
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather=parseXML(responseStr);
                    if(todayWeather!=null){
                        Log.d("myWeather",todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }


}
