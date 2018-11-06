package ua.com.adr.android.test_b;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import static ua.com.adr.android.test_b.Utils.hasIntenet;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    public static final String LINKS_ACTION = "ua.com.adr.android.myapplication.action.LINKS";

    String url = "", from = "";
    int status;
    private PendingIntent pendingIntent;

    RequestListener<Bitmap> requestListener;
    AlertDialog alertDialog;
    SecondReceiver bReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        from = intent.getStringExtra("from");
        status = intent.getIntExtra("status", 0);


        bReceiver = new SecondReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ua.com.adr.android.test_b.action.PICTURES");
        registerReceiver(bReceiver, filter);

        mImageView = (ImageView) findViewById(R.id.imageView);

        checkOpenFrom();
    }


    public void checkOpenFrom() {
        if (!(from == null) && !from.equals("")) {
            loadImage();
        } else {
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Внимание!");
            alertDialog.setMessage("Приложение В не является самостоятельным приложением и будет закрыто через 10 секунд");
            alertDialog.show();

            new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    alertDialog.setMessage("Приложение В не является самостоятельным приложением и " +
                            "будет закрыто через " + (millisUntilFinished / 1000) + " секунд");
                }

                @Override
                public void onFinish() {
                    finishAffinity();
                }
            }.start();
        }
    }

    public void saveImageBitmap(Bitmap image_bitmap, String image_name) {
        String root = Environment.getExternalStorageDirectory().toString();
        if (isStoragePermissionGranted()) { // check or ask permission
            File myDir = new File(root, "/BIGDIG/test/B");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            String fname = "Image-" + image_name + ".jpg";
            File file = new File(myDir, fname);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile(); // if file already exists will do nothing
                FileOutputStream out = new FileOutputStream(file);
                image_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                Log.d("NewTag", "ERROR ");
                e.printStackTrace();
            }
            // add image to galery
            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, new String[]{file.getName()}, null);
        }
    }

    public void loadImage() {
        if(hasIntenet(this))  {
            Glide
                    .with(this)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                    )
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mImageView.setImageDrawable(resource);
                            updateDb(1);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            updateDb(2);
                        }

                    });
        } else  {
            updateDb(3);
        }
    }

    public void updateDb(int mStatus) {
        Intent intent = new Intent();
        intent.setAction(LINKS_ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        if (from.equals("okButton")) {
            intent.putExtra("ua.com.adr.android.broadcast.Update", "INSERT");
            intent.putExtra("URL", url);
            intent.putExtra("Status", mStatus);
            sendBroadcast(intent);
        } else if (from.equals("history")) {
            if (status == 1 && mStatus == 1) {
                Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                saveImageBitmap(bitmap, "" + System.currentTimeMillis());

                startService();
            } else if (status == 2 || status == 3) {
                if (mStatus != status) {
                    intent.putExtra("ua.com.adr.android.broadcast.Update", "UPDATE");
                    intent.putExtra("URL", url);
                    intent.putExtra("Status", mStatus);
                    sendBroadcast(intent);
                }
            }
        }
    }

    public void startService() {
        Intent myIntent = new Intent(MainActivity.this,
                MyAlarmService.class);
        myIntent.putExtra("curentUrl", url);
        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        pendingIntent = PendingIntent.getService(MainActivity.this, iUniqueId,
                myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 15);

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), pendingIntent);

    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    public class SecondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            url = intent.getStringExtra("ua.com.adr.android.broadcast.Url");
            from = intent.getStringExtra("ua.com.adr.android.broadcast.From");
            status = intent.getIntExtra("ua.com.adr.android.broadcast.Status", 0);

            Intent selfIntent = new Intent(MainActivity.this, MainActivity.class);

            selfIntent.putExtra("url", url);
            selfIntent.putExtra("from", from);
            selfIntent.putExtra("status", status);

            startActivity(selfIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

}
