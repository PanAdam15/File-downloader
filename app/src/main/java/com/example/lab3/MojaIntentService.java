package com.example.lab3;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MojaIntentService extends IntentService {
    private static final String AKCJA_ZADANIE1 =
            "com.example.lab3.action.MojaIntentService";
    private static final String ID_KANALU = "POBIERZ_PLIk";
    public static final String POWIADOMOENIE = "com.exmaple.lab3.extra.DownloadFileReceiver";
    public static final String INFO = "info";
    private static final int ID_POWIADOMIENIA = 1;
    private NotificationManager mManagerPowiadomien;
    boolean CzyPobiera = false;
    PostepInfo postep;

    public static void uruchomUsluge(Context context, URL url){
        Intent zamiar = new Intent(context, MojaIntentService.class);
        zamiar.setAction(AKCJA_ZADANIE1);
        zamiar.putExtra("url", url);
        context.startService(zamiar);
    }
    public MojaIntentService(){
        super("MojaIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mManagerPowiadomien = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        przygotujKanalPowiadomien();
        startForeground(ID_POWIADOMIENIA, utworzPowiadomienie());

        if (intent != null) {
            final String action = intent.getAction();
            if(AKCJA_ZADANIE1.equals(action)) {
                URL url = (URL) intent.getExtras().getSerializable("url");
                wykonajZadanie(url);
            } else {
                Log.e("intent_service", "nieznana akcja");
            }
        } Log.d("intent_service", "usługa wykonała zadanie");
    }

    private void wykonajZadanie(URL url){
        postep = new PostepInfo(0);
        Log.i("Status",postep.getStatus().toString());
        InputStream strumienZSieci = null;
        FileOutputStream strumienDoPliku = null;

        try {
          //  URL url = new URL(mAdres);
            File plikRoboczy = new File(url.getFile());
            File plikWyjsciowy = new File(
                    Environment.getExternalStorageDirectory() +
                    File.separator + plikRoboczy.getName());
           // if(plikWyjsciowy.exists()) plikWyjsciowy.delete();


            HttpsURLConnection polaczenie = (HttpsURLConnection) url.openConnection();
            polaczenie.setRequestMethod("GET");

            DataInputStream czytnik = new DataInputStream(polaczenie.getInputStream());
            strumienDoPliku = new FileOutputStream(plikWyjsciowy.getPath());
            int ROZMIAR_BLOKU = 100;
            byte bufor[] = new byte[ROZMIAR_BLOKU];
            CzyPobiera = true;
            int pobrano = czytnik.read(bufor,0,ROZMIAR_BLOKU);
            int rozmiarPliku = polaczenie.getContentLength();
            int mPobranychBajtow = 0;
            postep.setmRozmiar(rozmiarPliku);
            postep.setStatus(PostepInfo.Status.Pobieranie_rozpoczete);
            Log.i("Status ",postep.getStatus().toString());
            while(pobrano != -1){
                strumienDoPliku.write(bufor,0,pobrano);
                mPobranychBajtow += pobrano;
                Log.i("Pobrano bajtow: ", String.valueOf(mPobranychBajtow));
                postep.setmPobranychBajtow(mPobranychBajtow);
                wyslijBroadcast(postep);
                mManagerPowiadomien.notify(ID_POWIADOMIENIA, utworzPowiadomienie());
                pobrano = czytnik.read(bufor, 0, ROZMIAR_BLOKU);
            }
            postep.setStatus(PostepInfo.Status.Pobieranie_zakonczone);
            Log.i("Status ",postep.getStatus().toString());
            CzyPobiera = false;
            if(strumienZSieci !=null){
                try{
                    strumienZSieci.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(polaczenie != null) polaczenie.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            postep.setStatus(PostepInfo.Status.Pobieranie_blad);
            Log.i("Status ",postep.getStatus().toString());
        }

    }

    private void przygotujKanalPowiadomien(){
        mManagerPowiadomien = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.app_name);
            NotificationChannel kanal = new NotificationChannel(ID_KANALU, name, NotificationManager.IMPORTANCE_LOW);
            mManagerPowiadomien.createNotificationChannel(kanal);

        }
    }

    private Notification utworzPowiadomienie(){
        Intent intencjaPowiadomienia = new Intent(this, MainActivity.class);
        //intencjaPowiadomienia.putExtra();
        TaskStackBuilder budowniczyStosu = TaskStackBuilder.create(this);
        budowniczyStosu.addParentStack(MainActivity.class);
        budowniczyStosu.addNextIntent(intencjaPowiadomienia);
        PendingIntent intencjaoczekujaca = budowniczyStosu.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder budowniczyPowiadomien = new Notification.Builder(this);
        budowniczyPowiadomien.setContentTitle("Pobieranie")
                                .setProgress(100, (int) wartoscPostepu(),false)
                                .setContentIntent(intencjaoczekujaca)
                                .setSmallIcon(R.drawable.ic_baseline_arrow_circle_down)
                                .setWhen(System.currentTimeMillis())
                                .setPriority(Notification.PRIORITY_HIGH);

        if(CzyPobiera){
            budowniczyPowiadomien.setOngoing(false);
        } else
            budowniczyPowiadomien.setOngoing(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            budowniczyPowiadomien.setChannelId(ID_KANALU);
        }

        return budowniczyPowiadomien.build();
    }

     void wyslijBroadcast(PostepInfo wartosc) {
        Intent intent = new Intent(POWIADOMOENIE);
        intent.putExtra(INFO, wartosc);
        sendBroadcast(intent);
    }

    float wartoscPostepu(){
    float progress = 0;
        if (postep != null && postep.getmRozmiar() != 0 && postep.getmPobranychBajtow() != 0) {
            return ((float) postep.getmPobranychBajtow() / postep.getmRozmiar() * 100);
        }
        return 0;
    }
}
