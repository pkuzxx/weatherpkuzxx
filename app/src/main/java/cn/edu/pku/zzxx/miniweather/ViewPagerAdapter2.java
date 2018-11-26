package cn.edu.pku.zzxx.miniweather;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administror on 2018/11/16.
 */

public class ViewPagerAdapter2 extends PagerAdapter {
    private List<View> views1;
    private Context context1;

    private ViewPagerAdapter2(List<View>views1,Context context1){
        this.views1=views1;
        this.context1=context1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views1.get(position));
        return views1.get(position);
    }

    @Override
    public int getCount() {
        return views1.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views1.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==object);
    }
}
