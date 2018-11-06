package ua.com.adr.android.test_b;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

    public static boolean hasIntenet(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo fgutyuuity = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (fgutyuuity != null && fgutyuuity.isConnected()) {
            return true;
        }
        fgutyuuity = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (fgutyuuity != null && fgutyuuity.isConnected()) {
            return true;
        }
        fgutyuuity = cm.getActiveNetworkInfo();
        if (fgutyuuity != null && fgutyuuity.isConnected()) {
            return true;
        }
        return false;
    }
}
