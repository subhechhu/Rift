package com.np.rift.infoScreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.np.rift.AppController;
import com.np.rift.R;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import java.util.ArrayList;
import java.util.List;


//This activity is called only once when the application is new or data is cleared from setting.
public class InfoActivity extends AppCompatActivity {
    private ViewPager viewPager;

    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removing the action bars to make acivity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_info);

        List<Fragment> fragments = getFragments(); //List of swipable fragments
        ViewPagerAdapter pageAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(pageAdapter);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) findViewById(R.id.indicator);
        inkPageIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private List<Fragment> getFragments() {
        /*
        * Add all the Fragments here
        * Parameters: InfoFragment.newInstanct(PositionOfFragment, Explanatory Text, Text Heading, Background Color,
        * Image to represent Context)
        * */
        List<Fragment> fList = new ArrayList<>();
        fList.add(InfoFragment.newInstance(0, AppController.getInstance().getString(R.string.personal_data),"",
                ContextCompat.getColor(this,R.color.color_tab_1), R.drawable.personal));
        fList.add(InfoFragment.newInstance(1, AppController.getInstance().getString(R.string.group_data),"",
                ContextCompat.getColor(this,R.color.color_tab_2), R.drawable.group_));
        fList.add(InfoFragment.newInstance(2,AppController.getInstance().getString(R.string.graph_data), "",
                ContextCompat.getColor(this,R.color.color_tab_4), R.drawable.graph));
        return fList;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        pos = savedInstanceState.getInt("position");
        viewPager.setCurrentItem(pos);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", pos);
    }
}
