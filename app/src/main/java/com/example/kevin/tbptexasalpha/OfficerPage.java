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
import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_page);

        progressDialog = ProgressDialog.show(this, "Loading Officer Information", "Please wait...");

        officers = new ArrayList<String>();
        images = new ArrayList<ImageView>();

        html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        OfficerData data = new OfficerData();
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

    //NOTE: Going to add potential code for getting office hours
    //doc.select("#sheets-viewport div div table tbody tr");
    //Iterate through
    //  Keep track of the current position of each column
    //  Skip the first row (just the days of the week; 2 columns for each day)
    //  Now for the current row:
    //      Get the 30 minute time block of this row
    //      For each new cell found
    //          Check to see if there is a name in the Text. If so, add to that officer's hours (Map?)
    //          If no name, still record the rows/columns the black space takes up.
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
