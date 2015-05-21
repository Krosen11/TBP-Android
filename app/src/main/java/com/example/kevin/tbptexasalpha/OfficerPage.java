package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;


public class OfficerPage extends Activity {

    private String officerName;
    private ImageView view;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_page);

        //Note: This is old code. Need to fix

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

    //Note: This is code from old LoginPage.java that parsed the Officer page for officer names
    /*
    String html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
    public void findOfficers(Document doc)
    {
        //Only called from TBPSiteThread
        threadLock.lock();
        try {
            Elements officerList = doc.select("#top [role=main] #post-18 div ul li div");//Gets officer nodes
            for (Element officer : officerList) {
                String info = officer.text();

                //Now we will extrapolate officer position, name, and contact info
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

                officers.add(information);
            }
        }
        finally {
            threadLock.unlock();
        }
    }
    */
}
