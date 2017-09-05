package com.np.rift.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import static com.np.rift.main.HomeActivity.FRAGMENT_COUNT;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class MainAdapter extends FragmentPagerAdapter {
    public MainAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Log.e("subhechhu", "MainAdapter int position: ");
        switch (position) {
            case 0:
                return new PersonalFragment();
//                return PersonalFragment.init(position);
            case 1:
                return new FragmentB();
//                return FragmentB.init(position);
            case 2:
                return new FragmentC();
//                return FragmentC.init(position);
        }
        return null;
    }


    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }
}
