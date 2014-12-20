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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

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
        flag1 = false;
        progressDialog = ProgressDialog.show(this, "Loading...", "Loading, please wait...");

        //Getting information for the login page
        getTBPMembers();
    }


    protected void getTBPMembers()
    {
        String html = "http://studentorgs.engr.utexas.edu/tbp/?page_id=18";
        String html2 = "https://docs.google.com/a/utexas.edu/spreadsheets/d/1cZK_XcDRVCatOfU825R-EO5WD7yrEh9m6rpKNLn-ePE/pubhtml?gid=302345735&single=true";
        ArrayUpdate update = new ArrayUpdate();

        //Starting a new thread to pull data from the internet
        TBPSiteThread thread = new TBPSiteThread(html, update);
        TBPCandidateThread thread2 = new TBPCandidateThread(html2, update);
        thread.execute();
        thread2.execute();

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
        private ArrayList<String[]> officers;
        private ArrayList<String> candidates;
        private ArrayList<String[]> vals;//This is for the candidate's non-name stats
        private ReentrantLock threadLock;//Lock created from the TBPSiteThread
        public ArrayUpdate()
        {
            officers = new ArrayList<String[]>();
            candidates = new ArrayList<String>();
            vals = new ArrayList<String[]>();
            threadLock = new ReentrantLock();
            doneFlag = false;
        }

        public void findOfficers(Document doc)
        {
            //Only called from TBPSiteThread
            threadLock.lock();
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

        public void findCandidates(Document doc)
        {
            threadLock.lock();
            int lineLock = 0;//Going to be used to get to actual candidate lines
            Elements candidateList = doc.select("#sheets-viewport #302345735 div table tbody tr");
            for (Element line : candidateList)
            {
                String word = line.text();
                //This line is a blank line if the only text is the row #
                boolean foundChar = false;
                for (int i = 0; i < word.length(); i++)
                {
                    char current = word.charAt(i);
                    if ((current > 'A' && current < 'Z') || (current > 'a' && current < 'z'))
                    {
                        //This means that this char is a letter, so we are done
                        foundChar = true;
                        break;
                    }
                }
                if (!foundChar)
                {
                    //This means we have reached a new section, so let's reset lineLock
                    lineLock = 0;
                    continue;
                }
                if (lineLock < 2)
                {
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
                for (int i = 5; i <= 23; i++)
                {
                    String value = cells.get(i).ownText();
                    if (value.isEmpty()) {value = "0";}
                    tempVals[counter] = value;
                    if (i > 12 && i % 2 == 1)
                    {
                        //Need to increment by 2 in this case
                        i++;
                    }
                    counter++;
                }

                candidates.add(name);
                vals.add(tempVals);
            }
            threadLock.unlock();
        }

        public void updateArray()
        {
            //Updates the autocomplete array
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> contact = new ArrayList<String>();
            if (officers.isEmpty() || candidates.isEmpty() || threadLock.isLocked())
            {
                return;
            }
            AutoCompleteTextView view = (AutoCompleteTextView) findViewById(R.id.user_name);
            for (String[] list : officers)
            {
                names.add(list[0] + ", " + list[1]);//Puts officer name and position in
                contact.add(list[2]);
            }
            for (String name : candidates)
            {
                names.add(name + ", Candidate");
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

                    if (name.contains("Candidate"))
                    {
                        //Go to a candidate page
                        intent = new Intent(LoginPage.this, CandidatePage.class);
                        intent.putExtra("name", name);

                        //Now we need the extra info
                        int index = nameList.indexOf(name);
                        index = index - 15;//Subtract out the officers
                        intent.putExtra("values", vals.get(index));
                    }
                    else
                    {
                        //Go to an officer page
                        intent = new Intent(LoginPage.this, OfficerPage.class);
                        intent.putExtra("name", name);

                        //Now I need to get the contact information
                        int index = nameList.indexOf(name);
                        intent.putExtra("contact", contactList.get(index));
                    }

                    startActivity(intent);
                }
            });
            progressDialog.dismiss();
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

        protected Void doInBackground(Void... voids)
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

        protected void onPostExecute(Void empty)
        {
            flag1 = true;
            System.out.println("Success");
        }
    }

    private class TBPCandidateThread extends AsyncTask<Void, Void, Void>
    {
        private String html;
        private ArrayUpdate update;
        public TBPCandidateThread(String link, ArrayUpdate temp)
        {
            html = link;
            update = temp;
        }

        protected Void doInBackground(Void... voids)
        {
            Void empty = null;
            try
            {
                //Setting the timeout to 10 seconds here
                Document doc = Jsoup.connect(html).timeout(10*1000).get();
                update.findCandidates(doc);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return empty;
        }

        protected void onPostExecute(Void empty)
        {
            while (!flag1){}
            update.updateArray();
            System.out.println("Success 2");
        }
    }
}
