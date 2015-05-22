package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilderFactory;


public class LoginPage extends Activity {

    private ProgressDialog progressDialog;
    private boolean flag1;//Flag for completion of TBPSiteThread

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        progressDialog = ProgressDialog.show(this, "Loading...", "Loading, please wait...");

        //Testing drawer
        String[] strings = new String[101];
        strings[0] = "Officers";
        for (int i = 1; i < 101; i++){
            strings[i] = "Hello " + Integer.toString(i);
        }

        DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        DrawerOnClickListener drawer = new DrawerOnClickListener(LoginPage.this, layout, drawerList);

        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, strings));
        drawerList.setOnItemClickListener(drawer);

        //Getting information for the login page
        getTBPMembers();
    }


    protected void getTBPMembers()
    {
        String html2 = "https://docs.google.com/a/utexas.edu/spreadsheets/d/1cZK_XcDRVCatOfU825R-EO5WD7yrEh9m6rpKNLn-ePE/pubhtml?gid=302345735&single=true";
        ArrayUpdate update = new ArrayUpdate();

        //Starting a new thread to pull data from the internet
        CandidateData data = new CandidateData(html2, update);
        Thread thread = new Thread(data);
        thread.start();

        while (thread.isAlive()) {}
        update.updateArray();

        int count = 0;
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
        private ArrayList<String> candidates;
        private ArrayList<String[]> vals;//This is for the candidate's non-name state
        public ArrayUpdate()
        {
            candidates = new ArrayList<String>();
            vals = new ArrayList<String[]>();
            doneFlag = false;
        }

        public void findCandidates(Document doc)
        {
            int lineLock = 0;//Going to be used to get to actual candidate lines
            Elements candidateList = doc.select("#sheets-viewport #302345735 div table tbody tr");
            for (Element line : candidateList) {
                String word = line.text();
                //This line is a blank line if the only text is the row #
                boolean foundChar = false;
                for (int i = 0; i < word.length(); i++) {
                    char current = word.charAt(i);
                    if ((current > 'A' && current < 'Z') || (current > 'a' && current < 'z')) {
                        //This means that this char is a letter, so we are done
                        foundChar = true;
                        break;
                    }
                }
                if (!foundChar) {
                    //This means we have reached a new section, so let's reset lineLock
                    lineLock = 0;
                    continue;
                }
                if (lineLock < 2) {
                    lineLock++;
                    continue;
                }

                //If we get here, we are on a candidate line
                Elements cells = line.getElementsByTag("td");
                String firstName = cells.get(0).ownText();
                String lastName = cells.get(1).ownText();
                String name = firstName.concat(" " + lastName);

                int counter = 0;
                String[] tempVals = new String[14];
                //Info from 5-13, 15, 17, 19, 21, 23
                for (int i = 5; i <= 23; i++) {
                    String value = cells.get(i).ownText();
                    if (value.isEmpty()) {
                        value = "0";
                    }
                    tempVals[counter] = value;
                    if (i > 12 && i % 2 == 1) {
                        //Need to increment by 2 in this case
                        i++;
                    }
                    counter++;
                }

                candidates.add(name);
                vals.add(tempVals);
            }
        }

        public void updateArray()
        {
            //Updates the autocomplete array
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> contact = new ArrayList<String>();
            AutoCompleteTextView view = (AutoCompleteTextView) findViewById(R.id.user_name);
            for (String name : candidates)
            {
                names.add(name);
            }

            final ArrayList<String> nameList = names;
            final ArrayList<String> contactList = contact;

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginPage.this, android.R.layout.simple_list_item_1, names);
            view.setAdapter(adapter);
            view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = null;
                    String name = ((TextView) view).getText().toString();

                    //Go to a candidate page
                    intent = new Intent(LoginPage.this, CandidatePage.class);
                    intent.putExtra("name", name);

                    //Now we need the extra info
                    int index = nameList.indexOf(name);
                    //index = index - 15;//Subtract out the officers
                    intent.putExtra("values", vals.get(index));

                    startActivity(intent);
                }
            });
            progressDialog.dismiss();
        }
    }

    private class CandidateData implements Runnable {
        String html;
        ArrayUpdate update;

        public CandidateData(String html, ArrayUpdate update){
            this.html = html;
            this.update = update;
        }

        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(html).timeout(10 * 1000).get();
                update.findCandidates(doc);
            }
            catch (IOException e){
                System.out.println("Couldn't connect to URL");
            }
        }
    }
}
