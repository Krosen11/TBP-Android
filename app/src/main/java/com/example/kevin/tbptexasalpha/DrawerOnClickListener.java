package com.example.kevin.tbptexasalpha;

import android.content.Intent;
import android.view.View;
import android.widget.*;
import android.app.Activity;

/**
 * Created by kevinrosen1 on 5/21/15.
 */
public class DrawerOnClickListener implements ListView.OnItemClickListener {
    Activity currentActivity;

    public DrawerOnClickListener(Activity activity){
        currentActivity = activity;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectMenu(view, i);
    }

    private void selectMenu(View view, int position){
        Intent intent = null;
        //String name = ((TextView) view).getText().toString();

        //Go to a candidate page
        intent = new Intent(currentActivity, OfficerPage.class);
        //intent.putExtra("name", name);

        //Now we need the extra info
        //int index = nameList.indexOf(name);
        //index = index - 15;//Subtract out the officers
        //intent.putExtra("values", vals.get(index));

        currentActivity.startActivity(intent);
    }
}
