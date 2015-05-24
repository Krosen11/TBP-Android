package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by kevinrosen1 on 5/23/15.
 */
public class CreateDrawer {
    public void initializeDrawer(Activity activity){
        String[] strings = {"Candidates", "Actives", "Officers", "Events", "Contact", "Corporate", "History"};

        DrawerLayout layout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) activity.findViewById(R.id.left_drawer);
        DrawerOnClickListener drawer = new DrawerOnClickListener(activity, layout, drawerList);

        drawerList.setAdapter(new ArrayAdapter<String>(activity,
                R.layout.drawer_list_item, strings));
        drawerList.setOnItemClickListener(drawer);
    }
}
