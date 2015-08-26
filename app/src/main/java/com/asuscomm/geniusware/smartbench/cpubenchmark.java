package com.asuscomm.geniusware.smartbench;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class cpubenchmark extends AppCompatActivity {

    final int primeCount = 100000;
    boolean inProgress = false;
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpubenchmark);

        resultTextView = (TextView) findViewById(R.id.resultTextView);
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
            new multiThreadBenchmarkAsync().execute("");
        }
    }

    public void onClickSingleThread(View v)
    {
        if(!inProgress) {
            inProgress = true;
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
            resultTextView.setText(String.valueOf(s + " ms"));
            inProgress = false;

            super.onPostExecute(s);
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
            resultTextView.setText(String.valueOf(s + " ms"));
            inProgress = false;

            super.onPostExecute(s);
        }
    }
}
