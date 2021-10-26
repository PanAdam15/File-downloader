package com.example.lab3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private TextView ViewRozmiar, ViewTyp, ViewAdres, ViewPobrane;
    private Button ButtonInfo, ButtonPobierz;
    private String adres_url;
    private ProgressBar postepBar;
    final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    String address = "https://mega.nz/file/DP4DmIZL#rmCLysa3eC6is5Daw3_xPzk8boLS2cMei7glIPRepxg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewRozmiar = findViewById(R.id.RozmiarInfoView);
        ViewTyp = findViewById(R.id.TypInfoView);
        ButtonInfo = findViewById(R.id.PobierzInfoButton);
        ViewAdres = findViewById(R.id.AdresInput);
        ButtonPobierz = findViewById(R.id.PobierzPlikButton);
        ViewPobrane = findViewById(R.id.BajtyInfoView);
        postepBar = findViewById(R.id.progressBar);
        postepBar.setMin(0);
        postepBar.setMax(100);
        postepBar.setProgress(0);
        ViewAdres.setText(address);

        ButtonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adres_url = ViewAdres.getText().toString();
                ZadanieAsynchroniczne zadanie = new ZadanieAsynchroniczne();
                zadanie.execute(adres_url);
            }
        });

        ButtonPobierz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pobierzPlik();
            }
        });

    }



    public class ZadanieAsynchroniczne extends AsyncTask<String, Integer, String> {

        int mRozmiar;
        String mTyp,mRozmiarstr;
        @Override
        protected String doInBackground(String... params){
           // int i = params[0].intValue();
            //kod

            HttpsURLConnection polaczenie = null;
            try {

                URL url = new URL(adres_url);
                polaczenie = (HttpsURLConnection) url.openConnection();
                polaczenie.setRequestMethod("GET");
                mRozmiar = polaczenie.getContentLength();
                mTyp = polaczenie.getContentType();
                mRozmiarstr = String.valueOf(mRozmiar);
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (polaczenie != null) polaczenie.disconnect();
            }

          //  publishProgress(new Integer[]{i+1});

            return mTyp;
        }

        @Override
        protected void onPostExecute(String result){
            TextView ViewRozmiar = findViewById(R.id.RozmiarInfoView);
            TextView ViewTyp = findViewById(R.id.TypInfoView);
                ViewTyp.setText(mTyp);
               ViewRozmiar.setText(mRozmiarstr);
        }
    }
    void pobierzPlik() {
        URL url = null;
        try {
            url = new URL(ViewAdres.getText().toString());
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                MojaIntentService.uruchomUsluge(MainActivity.this, url);
            } else {
                if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Brak dostepu", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions((Activity) this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int kodZadania, @NonNull String[] uprawnienia, @NonNull int[] decyzje) {
        super.onRequestPermissionsResult(kodZadania, uprawnienia, decyzje);
        switch (kodZadania) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (uprawnienia.length > 0 && uprawnienia[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && decyzje[0] == PackageManager.PERMISSION_GRANTED) {
                    pobierzPlik();
                } else {
                    Toast.makeText(this, "Brak dostepu", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    private BroadcastReceiver mOdbiorcaRozgloszen = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle tobolek = intent.getExtras();
            PostepInfo postep = tobolek.getParcelable(MojaIntentService.INFO);
            ViewPobrane.setText(String.valueOf(postep.getmPobranychBajtow()));
            float progres = 0;
            if (postepBar != null && postep.getmRozmiar() != 0 && postep.getmPobranychBajtow() != 0) {
                progres = ( (float) postep.getmPobranychBajtow() / postep.getmRozmiar() * 100);
            }
            postepBar.setProgress( (int) progres );
        }
    };

     @Override
    protected void onResume() {
         super.onResume();
         registerReceiver(
                 mOdbiorcaRozgloszen, new IntentFilter(MojaIntentService.POWIADOMOENIE));

     }
     @Override
    protected void onPause() {
         super.onPause();
         unregisterReceiver(mOdbiorcaRozgloszen);
     }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("rozmiar", ViewRozmiar.getText().toString());
        outState.putString("typ", ViewTyp.getText().toString());
        outState.putString("bajty", ViewPobrane.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ViewRozmiar.setText(savedInstanceState.getString("rozmiar"));
        ViewTyp.setText(savedInstanceState.getString("typ"));
        ViewPobrane.setText(savedInstanceState.getString("bajty"));
    }
}