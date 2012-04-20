package cz.cvut.fit.vyhliluk.vocards.activity.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.util.Const;

public abstract class TranslateTask extends
		AsyncTask<String, Void, List<String>> {

	private Language from;
	private Language to;

	// ================= STATIC ATTRIBUTES ======================

	private static final String KEY_TRANSLATIONS = "translations";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	public TranslateTask(Language from, Language to) {
		super();
		this.from = from;
		this.to = to;
	}

	// ================= OVERRIDEN METHODS ======================

	@Override
	protected List<String> doInBackground(String... params) {
		String url = String.format(Const.TRANSL_URI_TEMPLATE, from.getId(), to.getId(), URLEncoder.encode(params[0]));
		Log.d("uri", url);
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		List<String> res = new ArrayList<String>();

		try {
			HttpResponse resp = httpclient.execute(request);
			int status = resp.getStatusLine().getStatusCode();
			if (status == 200) { // OK
				InputStream is = resp.getEntity().getContent();
				String content = null;
				try {
					content = new java.util.Scanner(is).useDelimiter("\\A").next();
				} catch (java.util.NoSuchElementException e) {
				}
				JSONObject json = new JSONObject(content);
				if (json.has(KEY_TRANSLATIONS)) {
					JSONArray translations = json.getJSONArray(KEY_TRANSLATIONS);
					for (int i = 0; i < translations.length(); i++) {
						res.add(translations.getString(i));
					}
				}
			}
		} catch (ClientProtocolException e) {
			Log.e("Vocards Translation", "Error during translation: " + e.getMessage());
		} catch (IOException e) {
			Log.e("Vocards Translation", "Error during translation: " + e.getMessage());
		} catch (JSONException e) {
			Log.e("Vocards Translation", "Error during translation: " + e.getMessage());
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		return res;
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
