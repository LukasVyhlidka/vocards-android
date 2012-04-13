package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
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
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.ExportTask;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DictUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

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
		menu.add(none, MENU_EXPORT, none, R.string.export_menu_export);
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
		DictionaryDS.deleteDict(db, id);

		Toast.makeText(this, R.string.dict_list_dict_deleted_toast, Toast.LENGTH_SHORT).show();
		this.refreshListAdapter();
	}

	private void addDictActivity() {
		Intent i = new Intent(this, DictAddActivity.class);
		startActivity(i);
	}

	private void export(long[] dictIds) {
		Long[] ids = new Long[dictIds.length];
		for (int i = 0; i < dictIds.length; i++) {
			ids[i] = dictIds[i];
		}
		ExportTask task = new ExportTask(this);
		task.execute(ids);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	
}
