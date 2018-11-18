package cn.edu.pku.zzxx.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administror on 2018/11/15.
 */

public class Guide extends Activity implements ViewPager.OnPageChangeListener {
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    private ImageView[]dots;
    private int[]ids={R.id.iv1,R.id.iv2,R.id.iv3};
    private Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        initViews();
        initDots();
        btn = (Button) views.get(2).findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent i = new Intent(Guide.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    };
    void initDots(){
        dots = new ImageView[views.size()];
        for(int i = 0;i<views.size();i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }
    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.page1,null));
        views.add(inflater.inflate(R.layout.page2,null));
        views.add(inflater.inflate(R.layout.page3,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager)findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int a =0;a<ids.length;a++){
            if(a==position)
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            else
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
