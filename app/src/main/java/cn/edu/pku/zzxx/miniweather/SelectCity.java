package cn.edu.pku.zzxx.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zzxx.app.MyApplication;
import cn.edu.pku.zzxx.bean.City;

import static android.content.ContentValues.TAG;

/**
 * Created by Administror on 2018/10/24.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;
    private ListView mlistView;
    List<City> listcity;
    ArrayList<String> AL = new ArrayList<String>();
    String city;

    private EditText editText;
    private ArrayList<City> filterDataList= new ArrayList<City>();
    private ArrayAdapter adapter;
    private TextView mTextView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mTextView = (TextView)findViewById(R.id.t);
        mlistView = (ListView)findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,AL);
        mlistView.setAdapter(adapter);
        editText = (EditText)findViewById(R.id.edit_text);
        //根据输入框值的改变过滤搜索
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤列表
                AL.clear();
                String temp = "";
                Log.d("TAG",s.toString());
                //自己写
                String ss=s.toString().toLowerCase();
                Log.d("TAG",ss);
                //自己写
                for(City city:listcity){
                    String pinyin = city.getAllFristPY();
                    pinyin = pinyin.toLowerCase();
                    if(pinyin.contains(ss)){
                        AL.add(city.getCity());
                    }
                   else if(city.getCity().contains(s.toString())){
                        temp = city.getCity();
                        AL.add(temp);
                    }
                }
                for(int i = 0;i<AL.size();i++){
                    System.out.println(AL.get(i));
                }
                adapter.notifyDataSetChanged();
                Log.d("TAG",s.toString()+"------------------------------------------------");

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        initViews();
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                Toast.makeText(SelectCity.this,"你单击了："+i,Toast.LENGTH_SHORT).show();
                for(City c:listcity){
                    if(c.getCity().equals(AL.get(i))){
                        city = c.getNumber();
                        Log.d("LAG",city+"----------------------");
                    }
                }
            }
        });
    }





    //这是响应返回按钮点击事件的函数，
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();//主从线程消息传递，
                i.putExtra("cityCode",city);
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }

    }

    private void initViews(){
        MyApplication myapp = (MyApplication)getApplication();
        listcity = myapp.getCityList();//存放数据库中城市信息
        String temp="";
        //该循环从城市信息中获取城市名称和相应城市代码
        for (City city : listcity) {
            String cityName = city.getCity();

            temp = cityName;
            AL.add(temp);//将城市名称和城市代码放在数组里备用
        }
        //mlistView = (ListView)findViewById(R.id.list_view);
       // ArrayAdapter<String>adapter = new ArrayAdapter<String>(
                //this,android.R.layout.simple_list_item_1,AL);//用于ListView控件与数据之间适配
        //mlistView.setAdapter(adapter);
        //根据单击的城市名称，获取该城市的代码
        //mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
          //  @Override
           // public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
             //   Toast.makeText(SelectCity.this,"你单击了："+i,Toast.LENGTH_SHORT).show();
               // City s = listcity.get(i);
                //city=s.getNumber();
            //}
        //});
    }


}
