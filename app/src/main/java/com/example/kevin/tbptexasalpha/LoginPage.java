package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilderFactory;


public class LoginPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        //Getting information for the login page
        getTBPMembers();
    }


    protected void getTBPMembers()
    {
        String html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        ArrayUpdate update = new ArrayUpdate();

        //Starting a new thread to pull data from the internet
        TBPSiteThread thread = new TBPSiteThread(html, update);
        thread.execute();

        int count = 0;
        while(!update.doneFlag)
        {
            update.updateArray(this);
        }
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

    public class ArrayUpdate
    {
        public boolean doneFlag;
        private ArrayList<String[]> officers;
        private ReentrantLock threadLock;//Lock created from the TBPSiteThread
        public ArrayUpdate()
        {
            officers = new ArrayList<String[]>();
            threadLock = new ReentrantLock();
            doneFlag = false;
        }

        public void findOfficers(Document doc)
        {
            //Only called from TBPSiteThread
            threadLock.lock();
            Element body = doc.body();
            Elements officerList = doc.select("#top [role=main] #post-18 div ul li div");//Gets officer nodes
            for (Element officer : officerList)
            {
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
                String firstName = part2. substring(space2 + 1, part2.length());

                String name = firstName.concat(" " + lastName);
                String position = part2.substring(0, space2);
                String contact = info.substring(lastSpace + 1);
                String[] information = {name, position, contact};

                officers.add(information);
            }
            threadLock.unlock();
        }

        public void updateArray(Context context)
        {
            //Testing
            //Updates the autocomplete array
            ArrayList<String> names = new ArrayList<String>();
            if (officers.isEmpty() || threadLock.isLocked())
            {
                return;
            }
            AutoCompleteTextView view = (AutoCompleteTextView) findViewById(R.id.user_name);
            for (String[] list : officers)
            {
                names.add(list[0]);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names);
            view.setAdapter(adapter);
            doneFlag = true;
        }
    }

    private class TBPSiteThread extends AsyncTask<Void, Void, Void>
    {
        private String html;
        private ArrayUpdate update;
        public TBPSiteThread(String link, ArrayUpdate temp)
        {
            html = link;
            update = temp;
        }

        public Void doInBackground(Void... voids)
        {
            Void empty = null;
            try
            {
                //Setting the timeout to 10 seconds here
                Document doc = Jsoup.connect(html).timeout(10*1000).get();
                update.findOfficers(doc);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return empty;
        }

        public void onPostExecute(Void empty)
        {
            System.out.println("Success");
        }
    }
}
