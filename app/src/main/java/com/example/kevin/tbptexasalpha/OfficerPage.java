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
import java.util.ArrayList;


public class OfficerPage extends Activity {

    private String officerName;
    public String html;
    public ArrayList<String> officers;
    public ArrayList<ImageView> images;
    //private ImageView view;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_page);

        Intent intent = getIntent();
        progressDialog = ProgressDialog.show(this, "Welcome " + officerName, "Please wait...");

        officers = new ArrayList<String>();
        images = new ArrayList<ImageView>();

        html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        OfficerData data = new OfficerData();
        Thread thread = new Thread(data);
        thread.start();

        while(thread.isAlive()) {}

        for (String officer : officers){
            System.out.println(officer);
        }

        showOfficerInfo();

        //Note: This is old code. Need to fix
        /*

        Intent intent = getIntent();
        officerName = intent.getStringExtra("name");
        String contact = intent.getStringExtra("contact");

        int comma = officerName.indexOf(",");
        String position = officerName.substring(comma + 1);
        officerName = officerName.substring(0, comma);
        view = (ImageView) findViewById(R.id.officer_picture);

        TextView textView = (TextView) findViewById(R.id.officer_name);
        textView.setText(officerName);

        TextView textView2 = (TextView) findViewById(R.id.officer_position);
        textView2.setText(position);

        TextView textView3 = (TextView) findViewById(R.id.officer_contact);
        textView3.setText(contact);

        progressDialog = ProgressDialog.show(this, "Welcome " + officerName, "Please wait...");
        String link = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        OfficerInfo thread = new OfficerInfo(link);
        thread.execute();
        */
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

    public void findOfficers(Document doc)
    {
        Elements officerList = doc.select("#top [role=main] #post-18 div ul li div");//Gets officer nodes
        for (Element officer : officerList) {
            String info = officer.text();
            officers.add(info);

            //Now we will extrapolate officer position, name, and contact info
            /*
            int lastSpace = info.lastIndexOf(" ");
            String part1 = info.substring(0, lastSpace);

            int firstSpace = part1.lastIndexOf(" ");
            part1 = part1.substring(0, firstSpace);
            int nextSpace = part1.lastIndexOf(" ");
            part1 = part1.substring(0, nextSpace);

            int space1 = part1.lastIndexOf(" ");
            String lastName = part1.substring(space1 + 1, part1.length());
            String part2 = info.substring(0, space1);

            int space2 = part2.lastIndexOf(" ");
            String firstName = part2.substring(space2 + 1, part2.length());

            String name = firstName.concat(" " + lastName);
            String position = part2.substring(0, space2);
            String contact = info.substring(lastSpace + 1);
            String[] information = {name, position, contact};
            */
        }
        progressDialog.dismiss();
    }

    public void showOfficerInfo(){
        //TODO: need to divy up the work amongst multiple threads somehow

        int index = 0;
        LinearLayout layout = (LinearLayout) findViewById(R.id.officer_layout);

        for (String officer : officers){
            ImageView picture;
            TextView viewName, viewPos, viewContact;

            //Let's first start the picture retrieval process
            OfficerPicture pic = new OfficerPicture(officer);
            Thread picThread = new Thread(pic);
            picThread.start();

            while (picThread.isAlive()) {}

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
            viewName = new TextView(this);
            viewPos = new TextView(this);
            viewContact = new TextView(this);

            ImageView currImage = images.get(index);
            currImage.setId(4 * index);
            layout.addView(currImage);

            viewName.setText(name);
            viewName.setId(4 * index + 1);
            layout.addView(viewName);

            viewPos.setText(position);
            viewName.setId(4 * index + 2);
            layout.addView(viewPos);

            viewContact.setText(contact);
            viewName.setId(4 * index + 3);
            layout.addView(viewContact);

            TextView blankLine = new TextView(this);
            blankLine.setText("");
            layout.addView(blankLine);

            index++;
        }
    }

    private class OfficerPicture implements Runnable {
        //TODO: merge with OfficerData to speed up process (no need to connect to site twice!)
        String officer;

        public OfficerPicture(String officer){
            this.officer = officer;
        }

        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(html).timeout(10 * 1000).get();
                Elements officers = doc.select("#top [role=main] #post-18 div ul li");
                for (Element currOff : officers){
                    String nodeText = currOff.text();
                    if (!nodeText.equals(officer)) continue;
                    //This means that we are on the right node. So, let's try to get the picture 1st
                    Elements child = currOff.select("img");
                    String link = child.attr("src");
                    InputStream io = new java.net.URL(link).openStream();
                    Bitmap image = BitmapFactory.decodeStream(io);

                    ImageView view = new ImageView(OfficerPage.this);
                    view.setImageBitmap(image);
                    io.close();
                    images.add(view);

                    break;
                }
            }
            catch (IOException e){
                System.out.println("Couldn't connect to URL");
            }
        }
    }

    private class OfficerData implements Runnable {

        public OfficerData(){

        }

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

    /*

    private class OfficerInfo extends AsyncTask<Void, Void, Bitmap>
    {
        private String html;

        public OfficerInfo(String link)
        {
            html = link;
        }

        protected Bitmap doInBackground(Void... voids)
        {
            Bitmap image = null;
            try
            {
                Document doc = Jsoup.connect(html).timeout(10*1000).get();
                Elements officers = doc.select("#top [role=main] #post-18 div ul li");
                for (Element officer : officers)
                {
                    String nodeText = officer.text();
                    if (!nodeText.contains(officerName)) {continue;}
                    //This means that we are on the right node. So, let's try to get the picture 1st
                    System.out.print("Found");
                    Elements child = officer.select("img");
                    String link = child.attr("src");
                    InputStream io = new java.net.URL(link).openStream();
                    image = BitmapFactory.decodeStream(io);
                    io.close();
                    break;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap image)
        {
            view.setImageBitmap(image);
            System.out.println("Success");
            progressDialog.dismiss();
        }
    }
    */
}
