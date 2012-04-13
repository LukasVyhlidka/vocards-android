package cz.cvut.fit.vyhliluk.vocards.activity;

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
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.VocardsApp;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DictUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionarySerialization;

/**
 * Activity class
 * 
 * @author Lucky
 * 
 */
public class DictListActivity extends AbstractListActivity {
	// ================= STATIC ATTRIBUTES ======================

	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_DICT = 1;
	public static final int MENU_EXPORT = 2;

	public static final int CTX_MENU_DELETE = 50;

	// ================= INSTANCE ATTRIBUTES ====================

	// private ListView dictList = null;
	private EditText filterEdit = null;
	private MenuItem menuFilter = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_list);

		this.init();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.refreshListAdapter();
	}

	@Override
	protected void onPause() {
		super.onPause();

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		adapter.getCursor().close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, R.string.dict_list_menu_show_filter);
		menu.add(none, MENU_NEW_DICT, none, R.string.dict_list_menu_new_dict);
		menu.add(none, MENU_EXPORT, none, R.string.dict_list_menu_export);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SHOW_HIDE_FILTER:
				this.showHideFilter();
				break;
			case MENU_NEW_DICT:
				this.addDictActivity();
				break;
			case MENU_EXPORT:
				startActivityForResult(new Intent(this, DictMultiListActivity.class), DictMultiListActivity.REQUEST_DICT_LIST);
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == DictMultiListActivity.REQUEST_DICT_LIST && resultCode == RESULT_OK) {
			long[] ids = data.getLongArrayExtra(DictMultiListActivity.KEY_RESULT_LIST);
			this.export(ids);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		int none = Menu.NONE;

		menu.add(none, CTX_MENU_DELETE, none, res.getString(R.string.dict_list_ctx_delete_dict));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case CTX_MENU_DELETE:
				this.deleteDict(info.id);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Settings.setActiveDictionaryId(id);

		Intent i = new Intent(this, VocardsActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	/**
	 * Activity initialization
	 */
	private void init() {
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);

		this.registerForContextMenu(this.getListView());

		this.setListAdapter(DictUtil.createDictListAdapter(this));
	}

	/**
	 * Hides or Show a filter
	 */
	private void showHideFilter() {
		Resources res = this.getResources();

		if (this.filterEdit.getVisibility() == EditText.GONE) {
			this.filterEdit.setVisibility(EditText.VISIBLE);
			this.menuFilter.setTitle(res.getString(R.string.dict_list_menu_hide_filter));
		} else {
			this.filterEdit.setVisibility(EditText.GONE);
			this.menuFilter.setTitle(res.getString(R.string.dict_list_menu_show_filter));
		}
	}

	private void refreshListAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		DBUtil.closeExistingCursor(adapter.getCursor());
		Cursor c = DictionaryDS.getDictionaries(this.db);
		adapter.changeCursor(c);
	}

	private void deleteDict(long id) {
		this.db.delete(VocardsDataSource.DICTIONARY_TABLE, id);

		Toast.makeText(this, R.string.dict_list_dict_deleted_toast, Toast.LENGTH_SHORT).show();
		this.refreshListAdapter();
	}

	private void addDictActivity() {
		Intent i = new Intent(this, DictAddActivity.class);
		startActivity(i);
	}

	private void export(long[] dictIds) {
		// StringBuilder sb = new StringBuilder();
		// for (long id: dictIds) {
		// sb.append(id).append(", ");
		// }
		// Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

		Long[] ids = new Long[dictIds.length];
		for (int i = 0; i < dictIds.length; i++) {
			ids[i] = dictIds[i];
		}
		ExportTask task = new ExportTask(this);
		task.execute(ids);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	private class ExportTask extends AsyncTask<Long, String, JSONObject> {

		private Context ctx;
		private ProgressDialog pd;

		public ExportTask(Context ctx) {
			super();
			this.ctx = ctx;
			this.pd = new ProgressDialog(this.ctx, ProgressDialog.STYLE_SPINNER);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			this.pd.setTitle(R.string.dict_list_export_progress_title);
			this.pd.setMessage(res.getString(R.string.dict_list_export_progress_msg));
			this.pd.show();
		}

		@Override
		protected JSONObject doInBackground(Long... params) {
			VocardsDataSource db = new VocardsDataSource(VocardsApp.getInstance().getApplicationContext());
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
				Toast.makeText(ctx, R.string.settings_export_sdcard_not_mounted, Toast.LENGTH_LONG).show();
				return;
			}

			Log.d("result", result.toString());

			try {
				File dir = getExternalFilesDir();
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
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(f.getAbsolutePath()));
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Test");
				startActivity(Intent.createChooser(sendIntent, "Title:"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		public File getExternalFilesDir() {
			String packageName = getPackageName();
			File externalPath = Environment.getExternalStorageDirectory();
			return new File(externalPath.getAbsolutePath() + "/Android/data/"
					+ packageName + "/files");
		}

	}
}
