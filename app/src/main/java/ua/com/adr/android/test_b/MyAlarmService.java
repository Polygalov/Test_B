package ua.com.adr.android.test_b;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MyAlarmService extends Service {
    public static final String LINKS_ACTION = "ua.com.adr.android.myapplication.action.LINKS";
    String url;
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Toast.makeText(MyAlarmService.this,"Cсылка была удалена", Toast.LENGTH_SHORT).show();
        url = intent.getStringExtra("curentUrl");
        Intent linkIntent = new Intent();
        linkIntent.setAction(LINKS_ACTION);
        linkIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        linkIntent.putExtra("ua.com.adr.android.broadcast.Update", "DELETE");
        linkIntent.putExtra("URL", url);
        linkIntent.putExtra("Status", 1);

        sendBroadcast(linkIntent);
        stopSelf();

    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }
}
