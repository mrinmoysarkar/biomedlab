package com.mrinmoy.moy.heart_diagnosis;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by Mrinmoy on 11/17/2015.
 */
public class DSP_M {
    private static int Fs;
    DSP_M(int fs)
    {
        Fs = fs;
    }

    public static void moving_average(double[] data,int point_no)
    {
        int N=data.length;
        double[] mov_avg = new double[N];
        for(int i=0;i<N;i++)
        {
            int k=i-point_no/2;
            double sum = 0;
            for(int j=0;j<point_no;j++)
            {
                if(k>=0 && k<N)
                {
                    sum += data[k];
                }
                k++;
            }
            mov_avg[i] = sum/(double)point_no;
        }
        for(int i=0;i<N;i++)data[i]=mov_avg[i];
    }
    public static double var_SD_CoV(double[]data,int option)//option = 0 for variance, 1 for standard deviation, 2 for Coeddicient of variation
    {
        double mu = mean(data);
        int n=data.length;
        double sum=0;
        for(int i=0;i<n;i++)
        {
            sum += Math.pow((data[i]-mu),2.0);
        }
        if(option == 1)
            return Math.sqrt(sum / n);
        else if(option == 2)
            return Math.sqrt(sum / n)/mu;
        return sum/n;
    }
    public static double var_SD_CoV(int[]data,int option)//option = 0 for variance, 1 for standard deviation, 2 for Coefficient of variation
    {
        double mu = mean(data);
        int n=data.length;
        double sum=0;
        for(int i=0;i<n;i++)
        {
            sum += Math.pow((data[i]-mu),2.0);
        }
        if(option == 1)
            return Math.sqrt(sum / n);
        else if(option == 2)
            return Math.sqrt(sum / n)/mu;
        return sum/n;
    }
    public static double mean(double[] data)
    {
        int n=data.length;
        double sum = 0;
        for(int i = 0;i<n;i++)sum+=data[i];
        return sum/n;
    }
    public static double mean(int[] data)
    {
        int n=data.length;
        double sum = 0;
        for(int i = 0;i<n;i++)sum+=data[i];
        return sum/n;
    }

    public static void PSD(double[] data,int Nfft, double[] fft_val,double[] f,int Fs)
    {
        try {
            fft(data, Nfft, fft_val, f, Fs);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.i("stupid", "psd error: "+e.toString());
        }
        int N = data.length;
        int n = fft_val.length;
        for(int i =0;i<n;i++)
        {
            fft_val[i] = Math.pow(fft_val[i],2.0)/(double)N;
        }
    }
    public static void fft(double[] data,int Nfft, double[] fft_val,double[] f,int Fs)
    {
        try {
            Mat datam = Mat.zeros(1, Nfft, CvType.CV_32FC1);
            Mat dataout = datam.clone();
            int N = data.length;
            double df = (double) Fs / (double) Nfft;
            f[0] = 0;
            for (int i = 0; i < N; i++) {
                datam.put(0, i, data[i]);
                if (i <= Nfft / 2) {
                    f[i + 1] = f[i] + df;
                }
                if (i == (N - 1) && i <= Nfft / 2) {
                    for (int j = i + 1; j < Nfft / 2; j++) {
                        f[j + 1] += f[j] + df;
                    }
                }


            }
            Core.dft(datam, dataout, Core.DFT_COMPLEX_OUTPUT, Nfft);
            abs(dataout, fft_val);
            dataout.release();
            datam.release();
        }catch(Exception e)
        {
            e.printStackTrace();
            Log.i("stupid","fft error: "+e.toString());
        }
    }

    public static void abs(Mat m,double[] abs_data)
    {
        int N = (int) m.size().width;

        N = Math.min(N, abs_data.length);

        for(int i = 0;i<N;i++)
        {
            double[] ff = m.get(0, i);
            double d = ff[0]*ff[0]+ff[1]*ff[1];
            d=Math.sqrt(d);
            abs_data[i] = d;

        }
    }
}
