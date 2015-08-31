package com.asuscomm.geniusware.smartbench;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vungle.publisher.VunglePub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class cpubenchmark extends AppCompatActivity implements Serializable {

    private static final long serialVersionUID = 9876543210L;

    private final int primeCount = 100000;
    private boolean inProgress = false;
    private TextView resultTextView;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ProgressBar cpuBenchPB;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private ArrayList<Integer> multiThreadArrayList;
    private ArrayList<Integer> singleThreadArrayList;

    // get the VunglePub instance
    final VunglePub vunglePub = VunglePub.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpubenchmark);

        // get resources
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        mDrawerList = (ListView)findViewById(R.id.navList);
        cpuBenchPB = (ProgressBar) findViewById(R.id.cpuBenchProgressBar);
        cpuBenchPB.setVisibility(View.INVISIBLE);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        // create back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // perform requirements
        addDrawerItems();
        setupDrawer();

        // get your App ID from the app's main page on the Vungle Dashboard after setting up your app
        final String app_id = "smartbench";

        // initialize the Publisher SDK
        vunglePub.init(this, app_id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cpubenchmark, menu);
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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onClickMultiThread(View v)
    {
        if(!inProgress) {
            inProgress = true;
            cpuBenchPB.setVisibility(View.VISIBLE);
            new multiThreadBenchmarkAsync().execute("");
        }
    }

    public void onClickSingleThread(View v)
    {
        if(!inProgress) {
            inProgress = true;
            cpuBenchPB.setVisibility(View.VISIBLE);
            new singleThreadBenchmarkAsync().execute("");
        }
    }

    private class multiThreadBenchmarkAsync extends AsyncTask<String, Void,String>{

        @Override
        protected String doInBackground(String... params) {

            final Prime y = new Prime();
            final int processPerThread = primeCount/Runtime.getRuntime().availableProcessors();
            final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
            Thread []threads = new Thread[numberOfProcessors];

            // initialize threads
            for(int i = 0; i < numberOfProcessors; i++) {
                final int count = i;
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        y.totalPrimesIn(processPerThread*count,processPerThread*(count+1));
                    }
                });
            }

            // start timer
            long time = System.currentTimeMillis();

            // start threads
            for(int i = 0; i < numberOfProcessors; i++) {
                threads[i].start();
            }

            // wait till all threads are complete
            for(int i = 0; i < numberOfProcessors; i++) {
                try {
                    threads[i].join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            // end timer
            long res = System.currentTimeMillis() - time;

            return String.valueOf(res);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            resultTextView.setText(String.valueOf(s + " ms"));
            inProgress = false;
            cpuBenchPB.setVisibility(View.INVISIBLE);

            // save data
            //writeObject(multiThreadArrayList);
        }
    }
    private class singleThreadBenchmarkAsync extends AsyncTask<String, Void,String>{

        @Override
        protected String doInBackground(String... params) {

            final Prime y = new Prime();

            // start timer
            long time = System.currentTimeMillis();

            // Single Thread
            y.totalPrimesIn(0,primeCount);

            // end timer
            long res = System.currentTimeMillis() - time;

            return String.valueOf(res);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            resultTextView.setText(String.valueOf(s + " ms"));
            inProgress = false;
            cpuBenchPB.setVisibility(View.INVISIBLE);
        }
    }

    private void addDrawerItems() {
        String[] smartBenchSelection = { "CPU Benchmark", "CPU Stress Test" };
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smartBenchSelection);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if(position == 0) {
//                    Intent i = new Intent(getApplicationContext(), cpubenchmark.class);
//                    startActivity(i);
//                }
//                else if(position == 1) {
//                    Intent i = new Intent(getApplicationContext(), cpustress.class);
//                    startActivity(i);
//                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//        String fileName = "SmartBenchData.ser";
//        Context context = this.getApplicationContext();
//
//        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//        ObjectOutputStream os = new ObjectOutputStream(fos);
//        os.writeObject(this);
//        os.close();
//        fos.close();

        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("/sdcard/SmartBench/SmartBench.ser"))); //Select where you wish to save the file...
            oos.writeObject(out); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        }
        catch(Exception ex)
        {
            Log.v("Serialization Save Error : ", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

    }

    // Vungle override functions
    @Override
    protected void onPause() {
        super.onPause();
        vunglePub.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vunglePub.onResume();
    }

    public void playVungleAd(View v){
        vunglePub.playAd();
    }
}