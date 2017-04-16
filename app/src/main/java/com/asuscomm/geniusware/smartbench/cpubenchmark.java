package com.asuscomm.geniusware.smartbench;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class cpubenchmark extends AppCompatActivity {

    private final int primeCount = 100000;
    private boolean inProgress = false;
    private TextView resultTextView;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ProgressBar cpuBenchPB;
    private Button mMultiThreadButton;
    private Button mSingleThreadButton;
    private TextView mHeaderTextView;
    private WakeLock mCpuWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpubenchmark);

        // get resources
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        mDrawerList = (ListView)findViewById(R.id.navList);
        cpuBenchPB = (ProgressBar) findViewById(R.id.cpuBenchProgressBar);
        mMultiThreadButton = (Button) findViewById(R.id.multithreadButton);
        mSingleThreadButton = (Button) findViewById(R.id.singlethreadButton);
        mHeaderTextView = (TextView) findViewById(R.id.headerTextView);

        // hide progressbar
        cpuBenchPB.setVisibility(View.INVISIBLE);

        // perform requirements
        addDrawerItems();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartBenchWakeLock");
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

        return super.onOptionsItemSelected(item);
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

            mCpuWakeLock.acquire();

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
            long startTime = System.currentTimeMillis();

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
            long totalTime = System.currentTimeMillis() - startTime;

            mCpuWakeLock.release();

            return String.valueOf(totalTime);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            resultTextView.setText(String.valueOf(s + " ms"));
            inProgress = false;
            cpuBenchPB.setVisibility(View.INVISIBLE);
        }
    }

    private class singleThreadBenchmarkAsync extends AsyncTask<String, Void,String>{

        @Override
        protected String doInBackground(String... params) {

            mCpuWakeLock.acquire();

            final Prime y = new Prime();

            // start timer
            long time = System.currentTimeMillis();

            // Single Thread
            y.totalPrimesIn(0,primeCount);

            // end timer
            long res = System.currentTimeMillis() - time;

            mCpuWakeLock.release();

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
        String[] smartBenchSelection = { "CPU Benchmark", "Device Information" };
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smartBenchSelection);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    cpuBench();
                }
                else if (position == 1) {
                    deviceInfo();
                }
            }
        });
    }

    private void cpuBench(){
        resultTextView.setVisibility(View.VISIBLE);
        mSingleThreadButton.setVisibility(View.VISIBLE);
        mMultiThreadButton.setVisibility(View.VISIBLE);

        mHeaderTextView.setText("Time taken to calculate: ");
    }

    private void deviceInfo(){
        //mHeaderTextView.setVisibility(View.INVISIBLE);
        resultTextView.setVisibility(View.INVISIBLE);
        mSingleThreadButton.setVisibility(View.INVISIBLE);
        mMultiThreadButton.setVisibility(View.INVISIBLE);

        mHeaderTextView.setText(Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL.toUpperCase());
    }
}
