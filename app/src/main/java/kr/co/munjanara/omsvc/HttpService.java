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
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpService extends Service {
    public static final String TAG = MainActivity.class.getSimpleName();
//    ReadHttp mReadHttp;
    List<Phone> bankList = new ArrayList<>();
    List<Phone> userList = new ArrayList<>();

    HttpAsyncTask mHttpdAsyncTask;
    private Thread mThread = null;
    private String mVersion = null;



    static String[] mBankName = new String[0];
    static String[] mBankPhone = new String[0];;
    static String[] mUserName = new String[0];;
    static String[] mUserPhone = new String[0];;
    static int mnBankPhone = 0;
    static int mnUserPHone = 0;

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
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destoryed");

        if( mThread != null ) {
            mThread.interrupt();
            mThread = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        mReadHttp = new ReadHttp();
//        mReadHttp.onPostExecute("http...");

        if( "startForeground".equals(intent.getAction())) {
            startForegroundService();
        }

        if( mThread == null ) {
            mThread = new Thread("My Thread") {
                @Override
                public void run() {
                    for (int i = 0;; i++) {
                        try {
                            Thread.sleep(1000 * 10);
                        } catch (InterruptedException e) {
                            break;
                        }
                        readManagePhone();
//                        readTestData();
                        Log.d(TAG, "서비스 동작중 : " + i);
                    }
                }
            };
            mThread.start();
        }
        boolean run = true;


        return START_STICKY;
    }


    private void readTestData() {
//        new ReadHttp().execute("https://goo.gl/eIXu9l");

        new HttpAsyncTask().execute("https://goo.gl/eIXu9l");
    }

    private void readManagePhone() {
        String mBody = "bnkphn=<?xml version=\"1.0\"?>";
        mHttpdAsyncTask = new HttpAsyncTask();
        mHttpdAsyncTask.execute(mMunjanaraXMSHome + "mjnr/bankphone01.php");
    }

    public class HttpAsyncTask extends AsyncTask<String, Void, Document> {

        @Override
        protected void onPostExecute(Document doc) {
//            super.onPostExecute(s);

            Element munja = doc.getDocumentElement();

            NodeList items = munja.getElementsByTagName("VERSION");
            Node item = items.item(0);
            Node text = item.getFirstChild();
            String strVersion = text.getNodeValue();

            if( strVersion.equals(mVersion) == false ) {
                items = munja.getElementsByTagName("BANK");
                if(items.getLength()>0) {
                    mnBankPhone = 0;
                    bankList.clear();
                    for (int i = 0; i < items.getLength(); i++) {
                        Node banklist = items.item(i);
                        Element fstElmnt = (Element) banklist;
                        NodeList name = fstElmnt.getElementsByTagName("NAME");
                        NodeList phone = fstElmnt.getElementsByTagName("PHONE");
                        bankList.add(new Phone(name.item(0).getChildNodes().item(0).getNodeValue(), phone.item(0).getChildNodes().item(0).getNodeValue()));
                        mnBankPhone++;

                        Log.d(TAG, "SET NAME : " + name.item(0).getChildNodes().item(0).getNodeValue() + " PHONE : " + phone.item(0).getChildNodes().item(0).getNodeValue());
                    }
                }
                items = munja.getElementsByTagName("USER");
                if(items.getLength()>0) {
                    mnUserPHone = 0;
                    userList.clear();
                    for (int i = 0; i < items.getLength(); i++) {
                        Node userlist = items.item(i);
                        Element fstElmnt = (Element) userlist;
                        NodeList name = fstElmnt.getElementsByTagName("NAME");
                        NodeList phone = fstElmnt.getElementsByTagName("PHONE");
                        userList.add(new Phone(name.item(0).getChildNodes().item(0).getNodeValue(), phone.item(0).getChildNodes().item(0).getNodeValue()));
                        mnUserPHone++;

                        Log.d(TAG, "SET NAME : " + name.item(0).getChildNodes().item(0).getNodeValue() + " PHONE : " + phone.item(0).getChildNodes().item(0).getNodeValue());
                    }
                }

                mVersion = strVersion;
            } else {
                for (int i = 0; i < bankList.size(); i++) {
                    Log.d(TAG, "저장된 BANK " + i + " : N " + bankList.get(i).getmName() + " P : " + bankList.get(i).getmPhone());
                }
                for (int i = 0; i < userList.size(); i++) {
                    Log.d(TAG, "저장된 USER " + i + " : N " + userList.get(i).getmName() + " P : " + userList.get(i).getmPhone());
                }
            }

            mAfterJob.sendEmptyMessage(0);
        }

        @Override
        public Document doInBackground(String... strings) {
            URL     url;
            Document doc = null;

            try {
                url = new URL(strings[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return doc;
        }
    }

    Handler mAfterJob = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {


            switch(msg.what) {
                case 0:
                    Log.d(TAG, "VERSION : " + mVersion );

                    break;
            }
            super.handleMessage(msg);
        }
    };


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

}
