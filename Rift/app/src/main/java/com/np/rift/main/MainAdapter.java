package com.np.rift.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.np.rift.main.activityFragment.ActivityFragment;
import com.np.rift.main.groupFragment.MainGroupFragment;
import com.np.rift.main.personalFragment.MainPersonalFragment;

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
                return new MainPersonalFragment();
//                return MainPersonalFragment.init(position);
            case 1:
                return new MainGroupFragment();
//                return MainGroupFragment.init(position);
            case 2:
                return new ActivityFragment();
//                return ActivityFragment.init(position);
        }
        return null;
    }


    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }
}
