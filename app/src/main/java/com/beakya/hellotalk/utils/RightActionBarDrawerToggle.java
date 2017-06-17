package com.beakya.hellotalk.utils;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;

/**
 * Created by goodlife on 2017. 6. 15..
 */

public class RightActionBarDrawerToggle extends ActionBarDrawerToggle {
    private final DrawerLayout mDrawerLayout;
    public RightActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.mDrawerLayout = drawerLayout;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
            else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        }
        return false;
    }

}
