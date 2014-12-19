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
}
