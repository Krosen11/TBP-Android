package com.example.kevin.tbptexasalpha;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class OfficerPage extends Activity {

    //TODO: Also pull in officer office hours and display alongside the officer information

    public String html;
    public ArrayList<String> officers;
    public ArrayList<ImageView> images;
    private ProgressDialog progressDialog;
    private Map<String,ArrayList<OfficerTime>> officeHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_page);

        progressDialog = ProgressDialog.show(this, "Loading Officer Information", "Please wait...");

        officers = new ArrayList<String>();
        images = new ArrayList<ImageView>();
        officeHours = new LinkedHashMap<String, ArrayList<OfficerTime>>();

        html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        //OfficerData data = new OfficerData();
        OfficeHours data = new OfficeHours();
        Thread thread = new Thread(data);
        thread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_officer_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getOfficeHoursSheet(Document doc){
        Elements links = doc.select("#top div article div p:contains(TBP) [href]");
        for (Element link : links){
            //Only one of these
            return link.attr("href");
        }

        return "";//Will never be called, but needed
    }

    public void getOfficeHours(Document doc) {
        int currRow = 0;
        int[] columnInfo = new int[10];
        for (int i = 0; i < 10; i++){
            columnInfo[i] = 0;
        }
        Elements rows = doc.select("#sheets-viewport div div table tbody tr");
        for (Element row : rows){
            if (currRow == 0){
                currRow++;
                continue;
            }

            Elements blocks = row.getElementsByTag("td");
            String time = blocks.get(0).text();
            blocks.remove(0);//Since I already got this

            int currColumn = 0;//Corresponds to 1st column on Monday
            for (Element block : blocks){
                //This means that there is some new information about columns/times
                int[] updateInfo = updateColumnInfo(columnInfo, currColumn, block);
                currColumn = updateInfo[0];
                int numRowsSpanned = updateInfo[1];

                //Now that we've updated the column information, let's see if this block is for an officer
                String officer = block.text();
                if (!officer.isEmpty() && officer != null){
                    //Let's get the day of the week
                    String officeTime = getDay(currColumn) + " - " + time;

                    //Now we will add an Officer Time to our map
                    OfficerTime officerTime = new OfficerTime(officeTime, numRowsSpanned);
                    if (officeHours.containsKey(officer)) officeHours.get(officer).add(officerTime);
                    else {
                        ArrayList<OfficerTime> newList = new ArrayList<OfficerTime>();
                        newList.add(officerTime);
                        officeHours.put(officer, newList);
                    }
                }
            }

            //Now I will decrease the num
            for (int i = 0; i < 10; i++){
                columnInfo[i]--;
            }

            currRow++;
        }
        System.out.println(officeHours);
    }

    public void findOfficers(Document doc)
    {
        Elements officerList = doc.select("#top [role=main] #post-18 div ul li");// div");//Gets officer nodes
        for (Element officer : officerList) {
            String info = officer.text();
            officers.add(info);

            //Now let's get the picture
            Elements child = officer.select("img");
            String link = child.attr("src");
            try {
                InputStream io = new java.net.URL(link).openStream();
                Bitmap image = BitmapFactory.decodeStream(io);

                ImageView view = new ImageView(OfficerPage.this);
                view.setImageBitmap(image);
                io.close();
                images.add(view);
            }
            catch (IOException e){
                System.out.println("Bad URL");
            }
        }
    }

    public void showOfficerInfo() {

        int index = 0;
        LinearLayout layout = (LinearLayout) findViewById(R.id.officer_layout);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        for (String officer : officers) {
            TextView viewName, viewPos, viewContact;

            //Now we will get the officer information
            int lastSpace = officer.lastIndexOf(" ");
            String part1 = officer.substring(0, lastSpace);

            int firstSpace = part1.lastIndexOf(" ");
            part1 = part1.substring(0, firstSpace);
            int nextSpace = part1.lastIndexOf(" ");
            part1 = part1.substring(0, nextSpace);

            int space1 = part1.lastIndexOf(" ");
            String lastName = part1.substring(space1 + 1, part1.length());
            String part2 = officer.substring(0, space1);

            int space2 = part2.lastIndexOf(" ");
            String firstName = part2.substring(space2 + 1, part2.length());

            String name = firstName.concat(" " + lastName);
            String position = part2.substring(0, space2);
            String contact = officer.substring(lastSpace + 1);

            //Now let's display everything
            Future pic = threadPool.submit(new AddPic(images.get(index), layout));
            Future text1 = threadPool.submit(new AddText(name, layout));
            Future text2 = threadPool.submit(new AddText(position, layout));
            Future text3 = threadPool.submit(new AddText(contact, layout));

            index++;
        }
    }

    //Helper functions

    public int[] updateColumnInfo(int[] columnInfo, int currColumn, Element block){
        int numRowsSpanned, numColsSpanned;
        int[] returnVals = new int[2];

        String getRows = block.attr("rowspan");
        if (!getRows.isEmpty() && getRows != null) numRowsSpanned = Integer.parseInt(getRows);
        else numRowsSpanned = 1;

        String getCols = block.attr("colspan");
        if (!getCols.isEmpty() && getCols != null) numColsSpanned = Integer.parseInt(getCols);
        else numColsSpanned = 1;

        int tempColumn = currColumn;
        int colCounter = 0;
        for (int i = tempColumn; i < 10; i++){
            //Did loop this way so I could directly use i
            if (columnInfo[i] != 0) continue;//This means that we are not in the right column
            if (colCounter < numColsSpanned) {
                columnInfo[i] = numRowsSpanned;
                currColumn = i;//So that we can be accurate in our updates
                colCounter++;
            }
            else break;
        }

        currColumn -= (numColsSpanned-1);//This should put us on the correct starting column
        returnVals[0] = currColumn;
        returnVals[1] = numRowsSpanned;
        return returnVals;
    }

    public String getDay(int currColumn){
        String day = new String();
        switch(currColumn) {
            case 0:
            case 1:
                day = "Monday";
                break;
            case 2:
            case 3:
                day = "Tuesday";
                break;
            case 4:
            case 5:
                day = "Wednesday";
                break;
            case 6:
            case 7:
                day = "Thursday";
                break;
            case 8:
            case 9:
                day = "Friday";
                break;
        }

        return day;
    }

    //These classes are all of the threads used in this page

    //Adds a text view to the Layout
    private class AddText implements Runnable {
        LinearLayout layout;
        TextView view;
        String text;
        public AddText(String text, LinearLayout layout){
            this.text = text;
            this.layout = layout;
        }

        @Override
        public void run() {
            view = new TextView(OfficerPage.this);
            view.setText(text);
            //Can only add views on the UI Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.addView(view);
                }
            });
        }
    }

    //Adds a picture to the Layout
    private class AddPic implements Runnable {
        LinearLayout layout;
        ImageView view;
        public AddPic(ImageView view, LinearLayout layout){
            this.view = view;
            this.layout = layout;
        }

        @Override
        public void run() {
            //Can only add views on the UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.addView(view);
                }
            });
        }
    }

    private class OfficeHours implements Runnable {
        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(html).timeout(10 * 1000).get();
                String link = getOfficeHoursSheet(doc);
                System.out.println(link);
                doc = Jsoup.connect(link).timeout(10 * 1000).get();
                getOfficeHours(doc);
            }
            catch (IOException e){
                System.out.println("Couldn't connect to URL");
            }
        }
    }

    //This class will get the Officer info from the website
    private class OfficerData implements Runnable {

        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(html).timeout(10 * 1000).get();
                findOfficers(doc);
                showOfficerInfo();
                //Can only deal with ProgressDialogs on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
            catch (IOException e){
                System.out.println("Couldn't connect to URL");
            }
        }
    }
}
