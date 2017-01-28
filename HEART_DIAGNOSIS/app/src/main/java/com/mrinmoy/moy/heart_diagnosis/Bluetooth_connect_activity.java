package com.mrinmoy.moy.heart_diagnosis;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class Bluetooth_connect_activity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    Button back,ok;
    Switch bluetooth_switch;
    ListView bluetooth_devices_list;
    BluetoothAdapter bluetooth;
    private boolean bluetooth_on = false;
    public static final int BLUETOOTH_RESULTCODE = 0;
    ListAdapter device_list;
    String mac="";
    int N;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_config_layout);
        init_layout_component();
        bluetooth_init();
    }
    private void init_layout_component()
    {
        back = (Button)findViewById(R.id.btn_back_bConfi);
        ok = (Button)findViewById(R.id.btn_connect);
        bluetooth_switch = (Switch)findViewById(R.id.sw_bluetooth);
        bluetooth_devices_list = (ListView)findViewById(R.id.bluetooth_device_lists);
        back.setOnClickListener(Bluetooth_connect_activity.this);
        ok.setOnClickListener(Bluetooth_connect_activity.this);
        bluetooth_switch.setOnCheckedChangeListener(Bluetooth_connect_activity.this);
        bluetooth_devices_list.setOnItemClickListener(Bluetooth_connect_activity.this);
    }

    private void bluetooth_init()
    {
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        if(bluetooth!=null)
        {
            if(bluetooth.isEnabled())
            {
                bluetooth_on = true;
                bluetooth_switch.setChecked(true);
                update_device_list();
            }
            else
            {
                bluetooth_switch.setChecked(false);
                Toast.makeText(this,"Turn on Bluetooth Device",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this,"No bluetooth Device Found",Toast.LENGTH_SHORT).show();
        }
    }
    private void update_device_list()
    {
        Set<BluetoothDevice> bluetooth_devices = bluetooth.getBondedDevices();
        N = bluetooth_devices.size();
        String[] name_mac = new String[N];
        int i=0;
        for(BluetoothDevice d:bluetooth_devices)
        {
            name_mac[i] = d.getName()+"\n"+d.getAddress();
            i++;
            //Toast.makeText(this,name_mac[i],Toast.LENGTH_LONG).show();
        }
        //name_mac[i] = "nam"+"\n"+"pam";

        device_list = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,name_mac);
        bluetooth_devices_list.setAdapter(device_list);




    }
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btn_connect:
                if(bluetooth_on && !mac.isEmpty())
                {
                    Intent i = new Intent();
                    i.putExtra("mac", mac);
                    setResult(Activity.RESULT_OK,i);
                    finish();
                }
                else
                {
                    Toast.makeText(this,"Turn on Bluetooth Device or select a device",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_back_bConfi:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                update_device_list();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked)
        {
            if(!bluetooth_on) {
                //bluetooth.enable();
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i,1);
            }
            bluetooth_on = true;

            //Toast.makeText(this,"in switch on",Toast.LENGTH_LONG).show();

        }else
        {
            bluetooth.disable();
            bluetooth_on = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mac = device_list.getItem(position).toString().split("\n")[1];
        Log.e("stupid",mac);
        view.setBackgroundColor(Color.RED);
        for(int i=0;i<N;i++)
        {
            if(i!=position)
            parent.getChildAt(i).setBackgroundColor(Color.WHITE);
        }
    }
}
