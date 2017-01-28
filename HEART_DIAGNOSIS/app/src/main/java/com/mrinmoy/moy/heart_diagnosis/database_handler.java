package com.mrinmoy.moy.heart_diagnosis;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by USER on 11/17/2015.
 */
public class database_handler {
    public static final String dir_name="HEART_DIAGNOSIS";

    public static boolean save_data(double[] data,String filename)
    {
        File f;
        FileOutputStream fos;
        OutputStreamWriter osw;
        try
        {
            f = new File(Environment.getExternalStorageDirectory(),dir_name);
            if(f.isDirectory())
            {
                f = new File(Environment.getExternalStorageDirectory(),dir_name+"/"+filename);

                fos = new FileOutputStream(f,true);
                String s= "";
                for(int i = 0;i<data.length;i++)
                {
                    s += String.valueOf(data[i])+" ";
                }
                fos.write(s.getBytes());
                fos.close();
                Log.i("stupid","directory available");
            }else{
                f.mkdir();
                f = new File(Environment.getExternalStorageDirectory(),dir_name+"/"+filename);

                fos = new FileOutputStream(f,true);
                String s= "";
                for(int i = 0;i<data.length;i++)
                {
                    s += String.valueOf(data[i])+" ";
                }
                fos.write(s.getBytes());
                fos.close();
                Log.i("stupid","making directory");
            }

            Log.i("stupid", Environment.getExternalStorageDirectory().toString());
            return true;
        }catch(Exception e) {
            e.printStackTrace();
            Log.i("stupid","error");
            return false;
        }
    }
    public static boolean save_data(int[] data,String filename)
    {
        File f;
        FileOutputStream fos;
        OutputStreamWriter osw;
        try
        {
            f = new File(Environment.getExternalStorageDirectory(),dir_name);
            if(f.isDirectory())
            {
                f = new File(Environment.getExternalStorageDirectory(),dir_name+"/"+filename);

                fos = new FileOutputStream(f,true);
                String s= "";
                for(int i = 0;i<data.length;i++)
                {
                    s += String.valueOf(data[i])+" ";
                }
                fos.write(s.getBytes());
                fos.close();
                Log.i("stupid","directory available");
            }else{
                f.mkdir();
                f = new File(Environment.getExternalStorageDirectory(),dir_name+"/"+filename);

                fos = new FileOutputStream(f,true);
                String s= "";
                for(int i = 0;i<data.length;i++)
                {
                    s += String.valueOf(data[i])+" ";
                }
                fos.write(s.getBytes());
                fos.close();
                Log.i("stupid","making directory");
            }

            Log.i("stupid", Environment.getExternalStorageDirectory().toString());
            return true;
        }catch(Exception e) {
            e.printStackTrace();
            Log.i("stupid","error");
            return false;
        }
    }
    public static void delete_data(String filename)
    {
        File f;
        try {
            f = new File(Environment.getExternalStorageDirectory(), dir_name + "/" + filename);
            f.delete();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
