package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class OfficerPage extends Activity {

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

        //Initializing drawer
        CreateDrawer drawer = new CreateDrawer();
        drawer.initializeDrawer(this);

        officers = new ArrayList<String>();
        images = new ArrayList<ImageView>();
        officeHours = new LinkedHashMap<String, ArrayList<OfficerTime>>();

        html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        OfficerThread data = new OfficerThread();
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

            //Now let's get office hours from the map
            ArrayList<String> times = officerOfficeHours(firstName);

            //Now let's display everything
            threadPool.submit(new AddPic(images.get(index), layout));
            threadPool.submit(new AddText(name, layout));
            threadPool.submit(new AddText(position, layout));
            threadPool.submit(new AddText(contact, layout));
            threadPool.submit(new AddText("Office Hours: ", layout));
            for (String time : times){
                threadPool.submit(new AddText("\t\t\t\t" + time, layout));
            }

            index++;
        }
        threadPool.shutdown();
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

    public ArrayList<String> officerOfficeHours(String firstName){
        ArrayList<String> times = new ArrayList<String>();
        if (officeHours.containsKey(firstName)){
            //ASSUME: Office hours will always be labeled by first name only! (And spelled right...)
            for (OfficerTime time : officeHours.get(firstName)){
                String startTime = time.getStartTime();
                if (startTime.contains("noon")) startTime = startTime.replace("noon", "12");
                int blocks = time.getHours();//This will tell us how much time to add for our hours

                //Now let's try to format the time in a good way
                int offset = 0;
                int dashLoc = startTime.lastIndexOf('-');
                String endTime = startTime.substring(dashLoc+1);
                endTime = endTime.replaceAll(" ", "");
                if (endTime.contains(":")) {
                    offset = 50;
                    int colonLoc = endTime.indexOf(':');
                    endTime = endTime.substring(0, colonLoc);
                }

                offset = offset + (50*blocks) - 50;//Should allow us to rather easily calculate new end time
                int hours = offset / 100;
                boolean isColon = ((offset % 100) != 0);

                int currHour = Integer.parseInt(endTime);
                currHour += hours;
                currHour = (currHour / 13) + (currHour % 13);
                String newEndTime = Integer.toString(currHour);
                if (isColon) newEndTime = newEndTime + ":30";

                //Finally, let's recreate the time
                String finalTime = startTime.substring(0, dashLoc+1) + newEndTime;
                times.add(finalTime);
            }
        }

        //Let's now sort the array list in day order
        //TODO: Sort by times in case of being on the same day
        for (int i = times.size()-1; i >= 0; i--){
            String minTime = new String();
            int minDate = Integer.MAX_VALUE;
            int minIndex = -1;

            for (int j = i; j >= 0; j--){
                String currTime = times.get(j);
                int date = -1;
                if (currTime.contains("Monday")) date = 0;
                else if (currTime.contains("Tuesday")) date = 1;
                else if (currTime.contains("Wednesday")) date = 2;
                else if (currTime.contains("Thursday")) date = 3;
                else date = 4;

                if (date < minDate){
                    minDate = date;
                    minTime = currTime;
                    minIndex = j;
                }
            }

            //Now we must move the min from times to the front
            times.remove(minIndex);
            times.add(minTime);
        }

        return times;
    }

    //These classes are all of the threads used in this page

    private class OfficerThread implements Runnable {
        @Override
        public void run() {
            OfficeHours hours = new OfficeHours();
            OfficerData data = new OfficerData();
            Thread t1 = new Thread(hours);
            Thread t2 = new Thread(data);
            t1.start();
            t2.start();
            try {
                t1.join();
                t2.join();
            }
            catch (InterruptedException e){
                System.out.println("Threads interrupted");
            }
            showOfficerInfo();
            //Can only deal with ProgressDialogs on the UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
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
            }
            catch (IOException e){
                System.out.println("Couldn't connect to URL");
            }
        }
    }

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
}
