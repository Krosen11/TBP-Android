package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;


public class LoginPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        getTBPMembers();
    }


    protected void getTBPMembers()
    {
        String html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        new TBPSiteThread(html).execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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

    private class TBPSiteThread extends AsyncTask<Void, Void, Void>
    {
        private String html;
        public TBPSiteThread(String link)
        {
            html = link;
        }

        public Void doInBackground(Void... voids)
        {
            Void empty = null;
            try
            {
                //Setting the timeout to 10 seconds here
                Document doc = Jsoup.connect(html).timeout(10*1000).get();
                Element body = doc.body();
                Elements stuff = body.getElementsByClass("entry-wrap entry-content");
                for (Element current : stuff)
                {
                    if (current.tagName().equals("ul") && current.className().equals("x-block-grid three-up"))
                    {
                        Elements frames = current.getElementsByClass("x-block-grid-item");
                        for (Element frame : frames)
                        {
                            if(frame.children().size() > 0)
                            {
                                Element officerBox = frame.child(0);

                                Element officerPos = officerBox.child(0);
                                Element officerName = officerBox.child(1);
                                Element officerContact = officerBox.child(2);

                                String position = officerPos.ownText();
                                String name = officerName.ownText();
                                String contact = officerContact.ownText();
                            }
                        }
                    }
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return empty;
        }

        public void onPostExecute(Void unused)
        {
            System.out.println("Success");
        }
    }
}
