package cz.cvut.fit.vyhliluk.vocards.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.ExportTask;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
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
	public static final int CTX_MENU_EDIT = 51;
	public static final int CTX_MENU_SHOW_DESC = 52;

	public static final String EXTRAS_PARENT_DICT_ID = "parentId";

	// ================= INSTANCE ATTRIBUTES ====================

	// private ListView dictList = null;
	private EditText filterEdit = null;
	private MenuItem menuFilter = null;

	private ExportTask exportTask = null;
	private AlertDialog alertDialog = null;

	private Long parentDictId = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_list);

		Object o = getLastNonConfigurationInstance();
		if (o != null) {
			this.exportTask = (ExportTask) o;
			this.exportTask.attach(this);
		}

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

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this
				.getListAdapter();
		adapter.getCursor().close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		// this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none,
		// R.string.dict_list_menu_show_filter);
		menu.add(none, MENU_NEW_DICT, none, R.string.dict_list_menu_new_dict)
				.setIcon(R.drawable.icon_new);
		menu.add(none, MENU_EXPORT, none, R.string.export_menu_export).setIcon(
				R.drawable.icon_send);
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
			startActivityForResult(
					new Intent(this, DictMultiListActivity.class),
					DictMultiListActivity.REQUEST_DICT_LIST);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == DictMultiListActivity.REQUEST_DICT_LIST
				&& resultCode == RESULT_OK) {
			long[] ids = data
					.getLongArrayExtra(DictMultiListActivity.KEY_RESULT_LIST);
			this.export(ids);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		int none = Menu.NONE;

		menu.add(none, CTX_MENU_DELETE, none,
				res.getString(R.string.dict_list_ctx_delete_dict));
		menu.add(none, CTX_MENU_EDIT, none,
				res.getString(R.string.dict_list_ctx_edit_dict));
		menu.add(none, CTX_MENU_SHOW_DESC, none,
				res.getString(R.string.dict_list_ctx_show_descendant_dicts));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case CTX_MENU_DELETE:
			this.deleteDict(info.id);
			break;
		case CTX_MENU_EDIT:
			this.editDict(info.id);
			break;
		case CTX_MENU_SHOW_DESC:
			this.showDescendants(info.id);
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

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (this.alertDialog != null && this.alertDialog.isShowing()) {
			this.alertDialog.dismiss();
		}
		if (this.exportTask == null
				|| this.exportTask.getStatus().equals(Status.FINISHED)) {
			return super.onRetainNonConfigurationInstance();
		}

		this.exportTask.detach();
		return this.exportTask;
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	/**
	 * Activity initialization
	 */
	private void init() {
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);

		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				R.layout.inf_dictionary_item, null, new String[] {
						VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_NAME }, new int[] {
						R.id.nativeLangIcon, R.id.foreignLangIcon,
						R.id.languageText });

		ViewBinder listViewBinder = new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				String colName = cursor.getColumnName(columnIndex);
				if (VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG
						.equals(colName)
						|| VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG
								.equals(colName)) {
					ImageView img = (ImageView) view;
					Language lang = Language
							.getById(cursor.getInt(columnIndex));
					img.setImageResource(lang.getIconId());
					return true;
				} else {
					return false;
				}
			}
		};

		listAdapter.setViewBinder(listViewBinder);
		this.registerForContextMenu(this.getListView());
		this.setListAdapter(listAdapter);

		Intent i = this.getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			this.handleBundle(b);
		}
	}

	private void handleBundle(Bundle b) {
		if (b.containsKey(EXTRAS_PARENT_DICT_ID)) {
			this.parentDictId = b.getLong(EXTRAS_PARENT_DICT_ID);
			Cursor c = DictionaryDS.getById(this.db, this.parentDictId);
			c.moveToFirst();
			this.setTitle(getString(
					R.string.dict_list_children_title,
					c.getString(c
							.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_NAME))));
			DBUtil.closeCursor(c);
		}
	}

	/**
	 * Hides or Show a filter
	 */
	private void showHideFilter() {
		Resources res = this.getResources();

		if (this.filterEdit.getVisibility() == EditText.GONE) {
			this.filterEdit.setVisibility(EditText.VISIBLE);
			this.menuFilter.setTitle(res
					.getString(R.string.dict_list_menu_hide_filter));
		} else {
			this.filterEdit.setVisibility(EditText.GONE);
			this.menuFilter.setTitle(res
					.getString(R.string.dict_list_menu_show_filter));
		}
	}

	private void refreshListAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this
				.getListAdapter();
		DBUtil.closeExistingCursor(adapter.getCursor());
		// Cursor c = DictionaryDS.getDictionaries(this.db);
		Cursor c = DictionaryDS.getChildDictionaries(this.db, this.parentDictId);
		adapter.changeCursor(c);
	}

	private void deleteDict(long dictId) {
		int count = DictionaryDS.getWordCount(db, dictId);
		String title = getString(R.string.dict_list_dialog_delete_title, count);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(title)
				.setCancelable(false)
				.setPositiveButton(R.string.dict_list_dialog_delete_yes,
						new DeleteYesListener(dictId))
				.setNegativeButton(R.string.dict_list_dialog_delete_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		this.alertDialog = builder.create();
		this.alertDialog.show();
	}

	private void addDictActivity() {
		Intent i = new Intent(this, DictAddActivity.class);
		if (this.parentDictId != null) {
			i.putExtra(DictAddActivity.EXTRAS_PARENT_DICT_ID, this.parentDictId);
		}
		startActivity(i);
	}

	private void editDict(long id) {
		Intent i = new Intent(this, DictAddActivity.class);
		i.putExtra(DictAddActivity.EXTRAS_DICT_ID, id);
		startActivity(i);
	}

	private void showDescendants(long id) {
		Intent i = new Intent(this, DictListActivity.class);
		i.putExtra(EXTRAS_PARENT_DICT_ID, id);
		startActivity(i);
	}

	private void export(long[] dictIds) {
		Long[] ids = new Long[dictIds.length];
		for (int i = 0; i < dictIds.length; i++) {
			ids[i] = dictIds[i];
		}
		this.exportTask = new ExportTask(this);
		this.exportTask.execute(ids);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	private class DeleteYesListener implements DialogInterface.OnClickListener {

		private long dictId;

		public DeleteYesListener(long dictId) {
			super();
			this.dictId = dictId;
		}

		public void onClick(DialogInterface dialog, int which) {
			DictionaryDS.deleteDict(db, dictId);

			Toast.makeText(DictListActivity.this,
					R.string.dict_list_dict_deleted_toast, Toast.LENGTH_SHORT)
					.show();
			DictListActivity.this.refreshListAdapter();
		}

	}
}
