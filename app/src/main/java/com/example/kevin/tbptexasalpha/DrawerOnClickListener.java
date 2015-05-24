package com.example.kevin.tbptexasalpha;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.*;
import android.app.Activity;

/**
 * Created by kevinrosen1 on 5/21/15.
 */
public class DrawerOnClickListener implements ListView.OnItemClickListener {
    Activity currentActivity;
    Class transitionActivity;
    DrawerLayout layout;
    ListView drawer;
    DrawerLayout.DrawerListener closeListener;

    public DrawerOnClickListener(Activity activity, DrawerLayout layout, ListView view){
        this.layout = layout;
        drawer = view;
        currentActivity = activity;
        transitionActivity = null;
        layout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Don't care
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Don't care, for now
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //We will start our activity now, if it exists
                //TODO: check if there is an activity to transition to
                if (transitionActivity != null) {
                    Intent intent = null;
                    intent = new Intent(currentActivity, OfficerPage.class);
                    currentActivity.startActivity(intent);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                //Don't care
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectMenu(view, i);
    }

    private void selectMenu(View view, int position){
        if (position == 2) transitionActivity = OfficerPage.class;
        layout.closeDrawer(drawer);
    }
}
