package kr.co.munjanara.omsvc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpService extends Service {
    public static final String TAG = MainActivity.class.getSimpleName();
    ReadHttp mReadHttp;
    private Thread mThread = null;
    private final String mMunjanaraXMSHome = "https://ad150.dataq.co.kr:8943/";
    List<Phone> pboneList = new ArrayList<>();

    public HttpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mReadHttp = new ReadHttp();
        mReadHttp.onPostExecute("http...");

        if( "startForeground".equals(intent.getAction())) {
            startForegroundService();
        }

        if( mThread == null ) {
            mThread = new Thread("My Thread") {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        try {
                            Thread.sleep(1000 * 10);
                            Log.d(TAG, "COUNTING " + i );
//                            readManagePhone();
                        } catch (InterruptedException e) {
                            break;
                        }
                        Log.d(TAG, "서비스 동작중");
                    }
                }
            };
            mThread.start();
        }
        boolean run = true;


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destoryed");

        if( mThread != null ) {
            mThread.interrupt();
            mThread = null;
        }
    }

    private void startForegroundService() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "munjanara");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("입금 알림");
        builder.setContentText("입금일림 서비스 실행 중");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,0);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("munjanara", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT ));
        }
        startForeground(1, builder.build());
    }

    private void readManagePhone() {
        String mBody = "bnkphn=<?xml version=\"1.0\"?>";
        mReadHttp = new ReadHttp();
        mReadHttp.onPostExecute(mMunjanaraXMSHome + "mjnr/bankphone01.php");
    }

    public class ReadHttp extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();
        String  mResult = null;
        @Override
        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
            mAfterJob.sendEmptyMessage(0);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Request request = new Request.Builder()
                        .url(strings[0])
                        .build();
                Response reponse = client.newCall(request).execute();
                mResult = reponse.body().toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return mResult;
        }
    }

    Handler mAfterJob = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "MSG : " + msg.what );

            switch(msg.what) {
                case 0:

                    break;
            }
            super.handleMessage(msg);
        }
    };
}
