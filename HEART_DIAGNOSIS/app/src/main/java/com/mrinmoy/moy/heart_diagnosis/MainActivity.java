package com.mrinmoy.moy.heart_diagnosis;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    LinearLayout graph_layout_ppg, graph_layout_bpm, graph_layout_psd, graph_layout_hrv;
    public static final int BLUETOOTH_RESULTCODE = 0;
    BluetoothAdapter bluetooth;
    String mac = "";
    Handler data_handler;
    public static final int MESSAGE_READ = 1;
    public static final int SUCCESS_CONNECT = 2;
    bluetooth_connection_manager bconnection;
    String total_message = "";
    LineGraphSeries<DataPoint> series_ppg, series_bpm, series_psd, series_hrv;
    int ppg_index = 0, bpm_index = 0;
    TextView tv_bpm, tv_mean_sd_cov;

    private final int bpm_total_point = 100;
    private final int ppg_total_point = 500;
    int[] bpm_array = new int[bpm_total_point];
    double[] ppg_array = new double[ppg_total_point];

    int moving_avg_point_no = 5;
    double mov_sum = 0;
    int ppg_graph_index = 0;

    Switch data_save;
    boolean save = false;

    FloatingActionButton delete_database;
    int fab_pressed = 0;
    public final int Fs = 25;
    static {
        if(!OpenCVLoader.initDebug())
        {
            Log.i("stupid","opencv failed");
        }else
        {
            //System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
            Log.i("stupid", "opencv success");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ///
        init_graphs();
        init_information_panel();
        ///
    }

    private void init_information_panel() {
        tv_bpm = (TextView) findViewById(R.id.tv_bpm);
        tv_mean_sd_cov = (TextView) findViewById(R.id.tv_Mean_SD_Cov);
        data_save = (Switch) findViewById(R.id.sw_datasave);
        data_save.setOnCheckedChangeListener(MainActivity.this);
        delete_database = (FloatingActionButton) findViewById(R.id.fab);
        delete_database.setOnClickListener(MainActivity.this);
    }

    private void init_graphs() {
        graph_layout_ppg = (LinearLayout) findViewById(R.id.graph_ppg);
        graph_layout_bpm = (LinearLayout) findViewById(R.id.graph_bpm);
        graph_layout_psd = (LinearLayout) findViewById(R.id.graph_ppg_psd);
        graph_layout_hrv = (LinearLayout) findViewById(R.id.graph_hrv_geo);


        series_ppg = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0)
        });
        series_bpm = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0)
        });
        series_psd = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0)
        });
        series_hrv = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0)
        });

        GraphView graphView_ppg = new GraphView(this);
        GraphView graphView_bpm = new GraphView(this);
        GraphView graphView_psd = new GraphView(this);
        GraphView graphView_hrv = new GraphView(this);
        //graphView.setRotation(90);

        graph_layout_ppg.addView(graphView_ppg);
        graph_layout_bpm.addView(graphView_bpm);
        graph_layout_psd.addView(graphView_psd);
        graph_layout_hrv.addView(graphView_hrv);
        // graphView.setLayoutParams(new
        //    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        graphView_ppg.addSeries(series_ppg);
        graphView_bpm.addSeries(series_bpm);
        graphView_psd.addSeries(series_psd);
        graphView_hrv.addSeries(series_hrv);

        graphView_ppg.setTitle("PPG Signal Vs Time");
        graphView_bpm.setTitle("BPM Signal Vs Time");
        graphView_psd.setTitle("PPG After Filtering");
        graphView_hrv.setTitle("X[n+D] Vs X[n]");

        series_ppg.setThickness(2);
        series_bpm.setThickness(2);
        series_psd.setThickness(2);
        series_hrv.setThickness(2);

        series_ppg.setColor(Color.BLUE);
        series_bpm.setColor(Color.RED);
        series_psd.setColor(Color.GREEN);
        series_hrv.setColor(Color.MAGENTA);


        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumIntegerDigits(1);

        graphView_ppg.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graphView_bpm.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graphView_psd.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graphView_hrv.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));

        //StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView_ppg);
        //staticLabelsFormatter.setHorizontalLabels(new String[]{"Time"});
        //staticLabelsFormatter.setVerticalLabels(new String[]{"PPG"});
        //graphView_ppg.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graphView_ppg.getViewport().setXAxisBoundsManual(true);
        graphView_ppg.getViewport().setYAxisBoundsManual(true);
        graphView_ppg.getViewport().setScalable(false);
        graphView_ppg.getViewport().setMinX(0);
        graphView_ppg.getViewport().setMaxX(150);
        graphView_ppg.getViewport().setMinY(0);
        graphView_ppg.getViewport().setMaxY(5);
        graphView_bpm.getViewport().scrollToEnd();

        graphView_bpm.getViewport().setXAxisBoundsManual(true);
        graphView_bpm.getViewport().setYAxisBoundsManual(true);
        graphView_bpm.getViewport().setScalable(false);
        graphView_bpm.getViewport().setMinX(0);
        graphView_bpm.getViewport().setMaxX(40);
        graphView_bpm.getViewport().setMinY(0);
        graphView_bpm.getViewport().setMaxY(250);
        graphView_bpm.getViewport().scrollToEnd();

        graphView_psd.getViewport().setXAxisBoundsManual(true);
        graphView_psd.getViewport().setYAxisBoundsManual(true);
        graphView_psd.getViewport().setScalable(false);
        graphView_psd.getViewport().setMinX(0);
        graphView_psd.getViewport().setMaxX(150);
        graphView_psd.getViewport().setMinY(0);
        graphView_psd.getViewport().setMaxY(5);

        graphView_hrv.getViewport().setXAxisBoundsManual(true);
        graphView_hrv.getViewport().setYAxisBoundsManual(true);
        graphView_hrv.getViewport().setScalable(false);
        graphView_hrv.getViewport().setMinX(0);
        graphView_hrv.getViewport().setMaxX(250);
        graphView_hrv.getViewport().setMinY(0);
        graphView_hrv.getViewport().setMaxY(250);
        //graphView_hrv.getViewport().scrollToEnd();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (!mac.isEmpty()) {
                bconnection.cancel();
                bluetooth.disable();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent i = new Intent(MainActivity.this, Bluetooth_connect_activity.class);

            startActivityForResult(i, BLUETOOTH_RESULTCODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_RESULTCODE) {
            if (resultCode == RESULT_OK) {
                mac = data.getStringExtra("mac");
                Log.i("stupid", "ok");
                Log.i("stupid", mac);
                bluetooth = BluetoothAdapter.getDefaultAdapter();
                data_handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        handle_bluetooth_data(msg);
                    }

                };
                bconnection = new bluetooth_connection_manager(bluetooth, data_handler, mac);
                if (bconnection.init()) {
                    Toast.makeText(getApplicationContext(), "CONNECTION SUCCESSFUL", Toast.LENGTH_SHORT).show();
                    bconnection.start();
                } else {
                    Toast.makeText(getApplicationContext(), "CONNECTION UNSUCCESSFUL", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /////
    private void handle_bluetooth_data(Message msg) {


        switch (msg.what) {
            case SUCCESS_CONNECT:
                Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_READ: {
                byte[] readBuf = (byte[]) msg.obj;
                String data_stream = new String(readBuf);
                total_message += data_stream;
                if (total_message.length() > 700) {
                    //Log.i("stupid", "total_message");
                    //Log.i("stupid", total_message);

                    //ArrayList<Integer> signal = new ArrayList<Integer>();
                    ArrayList<Integer> bpm_time = new ArrayList<Integer>();
                    String[] data_array = total_message.split("\n");
                    int n = data_array.length - 1;
                    //Log.i("stupid", "s:" + n);
                    for (int i = 0; i < n; i++) {
                        String s = data_array[i];
                        //Log.i("stupid",s.length()+"");
                        if (s.length() > 1) {
                            char ch = s.charAt(0);
                            try {
                                s = s.substring(1, s.length() - 1);
                                //Log.i("stupid",ch+"");
                                if (ch == 'S') {
                                    //signal.add(Integer.parseInt(s));
                                    double x = Double.parseDouble(s);
                                    x = 5.0 * x / 1024.0;
                                    ppg_array[ppg_index] = x;
                                    ppg_index++;
                                    /*
                                    if(ppg_index >= moving_avg_point_no)
                                    {
                                        mov_sum =0;
                                        int indx1 = ppg_index - moving_avg_point_no/2;
                                        for(int ii = indx1;ii<indx1+moving_avg_point_no;ii++)
                                        {
                                            if(ii<500) {
                                                mov_sum += ppg_array[ii];
                                            }
                                        }
                                        x=mov_sum/moving_avg_point_no;
                                        ppg_graph_index++;
                                        DataPoint d = new DataPoint(ppg_graph_index, x);
                                        series_ppg.appendData(d, true, 501);

                                    }
                                    */
                                    DataPoint d = new DataPoint(ppg_index, x);
                                    series_ppg.appendData(d, true, 501);

                                    //Thread.sleep(50);
                                    if (ppg_index == 500) {
                                        if (save) {
                                            database_handler.save_data(ppg_array, "ppg_raw.txt");
                                        }
                                        //plot_psd(ppg_array);
                                        plot_smooth_PPG(ppg_array);
                                        ppg_graph_index = 0;
                                        ppg_index = 0;
                                        series_ppg.resetData(new DataPoint[]{new DataPoint(0, 2.5)});
                                    }
                                } else if (ch == 'B') {
                                    int x = Integer.parseInt(s);
                                    bpm_array[bpm_index] = x;
                                    bpm_index++;
                                    DataPoint d = new DataPoint(bpm_index, x);
                                    series_bpm.appendData(d, true, 101);
                                    tv_bpm.setText("Current BPM is " + x);
                                    if (bpm_index == 100) {
                                        if (save) {
                                            database_handler.save_data(bpm_array, "bpm.txt");
                                        }
                                        DecimalFormat df = new DecimalFormat("##0.00");
                                        double mean = DSP_M.mean(bpm_array);
                                        double sd = DSP_M.var_SD_CoV(bpm_array, 1);
                                        double cov = sd / mean;
                                        tv_mean_sd_cov.setText("Mean = " + df.format(mean) + " SD = " + df.format(sd) + "\nCoV = " + df.format(cov));
                                        bpm_index = 0;
                                        series_bpm.resetData(new DataPoint[]{new DataPoint(0, 60)});
                                        plot_hrv(bpm_array, 5);
                                    }
                                } else if (ch == 'Q') {
                                    bpm_time.add(Integer.parseInt(s));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i("stupid", "ERRor parsing string");
                            }
                        }
                    }
                    total_message = "";
                }
            }
            break;
        }
    }

    ;

    private void plot_smooth_PPG(double[] data)
    {
        int moving_average_span = 7;
        DSP_M.moving_average(data,moving_average_span);
        DataPoint[] dp = new DataPoint[]{};
        series_psd.resetData(dp);
        for (int i = 0; i < 150;i++) {
            DataPoint dpoint = new DataPoint(i, data[i+moving_average_span]);
            series_psd.appendData(dpoint, false, 150);
            //Log.i("stupid", "" + data[i]);
        }
    }
private void plot_psd(double[] data)
{
    int NFFT = 1024;
    double[] psd = new double[NFFT/2+1];
    double[] f= new double[NFFT/2+1];
    DSP_M.PSD(data,NFFT,psd,f,Fs);

    DataPoint[] dp = new DataPoint[]{};
    series_psd.resetData(dp);
    for (int i = 0; i <= 3*NFFT/Fs; i++) {
        DataPoint dpoint = new DataPoint(f[i], psd[i]);
        series_psd.appendData(dpoint, false, 3*NFFT/Fs+1);
        Log.i("stupid", "" + psd[i]);
    }

}
    private void plot_hrv(int[] data, int delay) {
        int N = data.length - delay;

        if (N <= 0) return;
        int[] x_point = new int[N];
        int[] y_point = new int[N];
        x_point[0] = data[0];
        y_point[0] = data[delay];
        for (int i = 1; i < N; i++) {
            x_point[i] = data[i];
            y_point[i] = data[i + delay];
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N - i - 1; j++) {
                if (x_point[j] > x_point[j + 1]) {
                    int tem = x_point[j];
                    x_point[j] = x_point[j + 1];
                    x_point[j + 1] = tem;

                    tem = y_point[j];
                    y_point[j] = y_point[j + 1];
                    y_point[j + 1] = tem;
                }
            }
        }
        DataPoint[] dp = new DataPoint[]{};
        series_hrv.resetData(dp);
        for (int i = 0; i < N; i++) {
            DataPoint dpoint = new DataPoint(x_point[i], y_point[i]);
            series_hrv.appendData(dpoint, false, N);
            //Log.i("stupid", "" + x_point[i]);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            save = true;
        } else {
            save = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if(save)
                {
                    Toast.makeText(getApplicationContext(), "Turn off save button first", Toast.LENGTH_SHORT).show();
                    fab_pressed = 0;
                    return;
                }
                if (fab_pressed == 0) {
                    fab_pressed++;
                    Toast.makeText(getApplicationContext(), "Press again to delete Database", Toast.LENGTH_SHORT).show();
                } else {
                    fab_pressed = 0;
                    database_handler.delete_data("ppg_raw.txt");
                    database_handler.delete_data("bpm.txt");
                    Toast.makeText(getApplicationContext(), "Database Deleted", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    /////
}
