package cz.cvut.fit.vyhliluk.vocards.activity.task;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.ImportActivity;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.StorageUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionarySerialization;

public class ImportTask extends AsyncTask<File, Integer, Integer> {

	// ================= STATIC ATTRIBUTES ======================

	public static final int RESULT_OK = 0;

	// ================= INSTANCE ATTRIBUTES ====================

	private ImportActivity ctx;
	private ProgressDialog pd;
	private Resources res;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	public ImportTask(ImportActivity ctx) {
		super();
		this.ctx = ctx;
		this.pd = new ProgressDialog(this.ctx, ProgressDialog.STYLE_HORIZONTAL);
		this.res = this.ctx.getResources();
	}

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	@Override
	protected Integer doInBackground(File... params) {
		VocardsDataSource db = new VocardsDataSource(this.ctx);
		this.pd.setMax(params.length);

		boolean ok = false;
		try {
			db.open();
			db.begin();
			
			for (File f : params) {
				String content = StorageUtil.readEntireFile(f);
				JSONObject root = new JSONObject(content);
				JSONArray dicts = root.getJSONArray(DictionarySerialization.KEY_DICTIONARY_LIST);
				for (int i = 0; i < dicts.length(); i++) {
					JSONObject dict = dicts.getJSONObject(i);
					DictionarySerialization.importDictionary(db, dict);
				}
			}
			ok = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (VocardsException ex) {
			ex.printStackTrace();
		} finally {
			if (ok) {
				db.commit();
			} else {
				db.rollback();
			}
			db.close();
		}

		return RESULT_OK;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

		if (this.pd.isShowing()) {
			this.pd.dismiss();
		}
		
		Toast.makeText(this.ctx, R.string.import_activity_done, Toast.LENGTH_LONG).show();
		this.ctx.finish();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		this.pd.setTitle(R.string.import_progress_title);
		this.pd.setMessage(res.getString(R.string.import_progress_msg));
		this.pd.show();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

	}

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================
	
	public void attach(ImportActivity ctx) {
		this.ctx = ctx;
		this.pd = new ProgressDialog(this.ctx, ProgressDialog.STYLE_HORIZONTAL);
		this.res = this.ctx.getResources();
	}
	
	public void detach() {
		this.ctx = null;
		this.pd.dismiss();
		this.pd = null;
		this.res = null;
	}

	// ================= INNER CLASSES ==========================

}
