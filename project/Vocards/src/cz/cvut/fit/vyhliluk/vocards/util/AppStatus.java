package cz.cvut.fit.vyhliluk.vocards.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AppStatus {

	public static Boolean isOnline(Context con) {
		boolean connected = false;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
			return connected;
		} catch (Exception e) {
			Log.v("connectivity", e.toString());
		}

		return connected;
	}
}