package cz.cvut.fit.vyhliluk.vocards.activity.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.StorageUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionarySerialization;

public class ExportTask extends AsyncTask<Long, String, JSONObject> {

	private Context ctx;
	private ProgressDialog pd;
	private Resources res;

	public ExportTask(Context ctx) {
		super();
		this.ctx = ctx;
		this.pd = new ProgressDialog(this.ctx, ProgressDialog.STYLE_SPINNER);
		this.res = this.ctx.getResources();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		this.pd.setTitle(R.string.export_progress_title);
		this.pd.setMessage(res.getString(R.string.export_progress_msg));
		this.pd.show();
	}

	@Override
	protected JSONObject doInBackground(Long... params) {
		VocardsDataSource db = new VocardsDataSource(this.ctx);
		JSONObject root = new JSONObject();
		JSONArray dictArray = new JSONArray();

		try {
			db.open();
			for (long id : params) {
				JSONObject jsonDict = DictionarySerialization.getDictionaryJson(db, id);
				dictArray.put(jsonDict);
			}

			root.put(DictionarySerialization.KEY_DICTIONARY_LIST, dictArray);
		} catch (JSONException ex) {
			Log.e("Error", ex.getLocalizedMessage());
		} finally {
			db.close();
		}
		return root;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);

		if (this.pd.isShowing()) {
			this.pd.dismiss();
		}
		
		if (! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(ctx, R.string.export_sdcard_not_mounted, Toast.LENGTH_LONG).show();
			return;
		}

		try {
			File dir = StorageUtil.getExternalTempDir(this.ctx);
			dir.mkdirs();
			
			File f = File.createTempFile(
					DictionarySerialization.EXPORT_FILE_PREFIX, 
					DictionarySerialization.EXPORT_FILE_SUFFIX,
					dir);
			FileWriter fw = new FileWriter(f);
			
			fw.write(result.toString());
			fw.close();
			
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType(DictionarySerialization.VOCARDS_MIME);
			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+f));
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.export_send_subject));
			sendIntent.putExtra(Intent.EXTRA_TEXT, res.getString(R.string.export_send_text));
			this.ctx.startActivity(Intent.createChooser(sendIntent, res.getString(R.string.export_select_app_title)));
		} catch (IOException ex) {
			Log.e("error", ex.getMessage());
			Toast.makeText(ctx, R.string.export_file_write_error, Toast.LENGTH_LONG).show();
		}
	}

	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================

	//================= STATIC METHODS =========================

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
