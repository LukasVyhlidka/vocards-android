package cz.cvut.fit.vyhliluk.vocards.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.ExportTask;
import cz.cvut.fit.vyhliluk.vocards.core.ParentIsDescendantException;
import cz.cvut.fit.vyhliluk.vocards.core.ParentIsTheSameException;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
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
	public static final int CTX_MENU_MOVE_UNDER = 53;
	public static final int CTX_MENU_MOVE_ROOT = 54;

	// public static final String EXTRAS_PARENT_DICT_ID = "parentId";
	public static final String EXTRAS_MESSAGE = "message";
	public static final String EXTRAS_ONLY_DICT_SELECTION = "onlyDictSelection";

	public static final String KEY_RESULT_DICT_ID = "resDictId";
	
	private static final int REQUEST_PARENT_DICT = 100;

	// ================= INSTANCE ATTRIBUTES ====================

	// private ListView dictList = null;
	private EditText filterEdit = null;
	private MenuItem menuFilter = null;
	private View goUpView = null;
	private TextView parentFolderNameText = null;

	private ExportTask exportTask = null;
	private AlertDialog alertDialog = null;

	private Long parentDictId = null;

	/**
	 * If this is true, this activity has no menu and only returns the id of the
	 * dictionary
	 */
	private boolean onlyDictSelection = false;
	
	/**
	 * This is used for storing the moved dictionary id until user selects parent dictionary.
	 */
	private Long movedDictionaryId = null;

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

		DictListAdapter adapter = (DictListAdapter) this.getListAdapter();
		adapter.getCursor().close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		if (!this.onlyDictSelection) {
			menu.add(none, MENU_NEW_DICT, none, R.string.dict_list_menu_new_dict)
					.setIcon(R.drawable.icon_new);
			menu.add(none, MENU_EXPORT, none, R.string.export_menu_export).setIcon(
					R.drawable.icon_send);
		}
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

		if (requestCode == DictMultiListActivity.REQUEST_DICT_LIST && resultCode == RESULT_OK) {
			long[] ids = data
					.getLongArrayExtra(DictMultiListActivity.KEY_RESULT_LIST);
			this.export(ids);
		} else if (requestCode == REQUEST_PARENT_DICT && resultCode == RESULT_OK) {
			long id = data.getExtras().getLong(KEY_RESULT_DICT_ID);
			this.moveUnderDictionary(this.movedDictionaryId, id);
			this.movedDictionaryId = null;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		int none = Menu.NONE;

		menu.add(none, CTX_MENU_DELETE, none, R.string.dict_list_ctx_delete_dict);
		menu.add(none, CTX_MENU_EDIT, none, R.string.dict_list_ctx_edit_dict);
		menu.add(none, CTX_MENU_MOVE_UNDER, none, R.string.dict_list_ctx_move_dict_under);
		menu.add(none, CTX_MENU_MOVE_ROOT, none, R.string.dict_list_ctx_move_root);

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
			case CTX_MENU_MOVE_UNDER:
				this.movedDictionaryId = info.id;
				Intent i = new Intent(this, DictListActivity.class);
				i.putExtra(EXTRAS_MESSAGE, res.getString(R.string.dict_list_select_parent_title));
				i.putExtra(EXTRAS_ONLY_DICT_SELECTION, true);
				startActivityForResult(i, REQUEST_PARENT_DICT);
				break;
			case CTX_MENU_MOVE_ROOT:
				this.moveToRoot(info.id);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent i = getIntent();
		i.putExtra(KEY_RESULT_DICT_ID, id);

		setResult(RESULT_OK, i);
		finish();
		overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (this.alertDialog != null && this.alertDialog.isShowing()) {
			this.alertDialog.dismiss();
		}
		if (this.exportTask == null || this.exportTask.getStatus().equals(Status.FINISHED)) {
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
		this.goUpView = findViewById(R.id.goUp);
		this.parentFolderNameText = (TextView) findViewById(R.id.parentFolderName);

		if (!this.onlyDictSelection) {
			this.registerForContextMenu(this.getListView());
		}
		DictListAdapter listAdapter = new DictListAdapter(this, null);
		this.setListAdapter(listAdapter);

		this.goUpView.setOnClickListener(this.goUpClickListener);

		Intent i = this.getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			this.handleBundle(b);
		}
	}

	private void handleBundle(Bundle b) {
		if (b.containsKey(EXTRAS_MESSAGE)) {
			this.setTitle(b.getString(EXTRAS_MESSAGE));
		}

		if (b.containsKey(EXTRAS_ONLY_DICT_SELECTION)) {
			this.onlyDictSelection = b.getBoolean(EXTRAS_ONLY_DICT_SELECTION);
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
		DictListAdapter adapter = (DictListAdapter) this
				.getListAdapter();
		DBUtil.closeExistingCursor(adapter.getCursor());
		Cursor c = DictionaryDS.getChildDictionaries(this.db, this.parentDictId);
		adapter.changeCursor(c);

		this.handleUpIcon();
	}

	private void handleUpIcon() {
		if (this.parentDictId != null) {
			Cursor pDict = DictionaryDS.getById(this.db, this.parentDictId);
			pDict.moveToFirst();
			String dictName = pDict.getString(pDict.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_NAME));
			String text = getString(R.string.dict_list_children_title, dictName);

			this.parentFolderNameText.setText(text);
			this.parentFolderNameText.setVisibility(View.VISIBLE);
			this.goUpView.setVisibility(View.VISIBLE);

			DBUtil.closeCursor(pDict);
		} else {
			this.parentFolderNameText.setVisibility(View.GONE);
			this.goUpView.setVisibility(View.GONE);
		}
	}

	private void deleteDict(long dictId) {
		int count = DictionaryDS.getWordCount(db, dictId);
		String title = getString(R.string.dict_list_dialog_delete_title, count);

		View checkView = getLayoutInflater().inflate(R.layout.inf_delete_dict_dialog, null);
		CheckBox checkBox = (CheckBox) checkView.findViewById(R.id.checkboxDelDesc);
		DeleteYesListener lsnr = new DeleteYesListener(dictId, checkBox);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(title)
				.setCancelable(false)
				.setPositiveButton(R.string.dict_list_dialog_delete_yes, lsnr)
				.setNegativeButton(R.string.dict_list_dialog_delete_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setView(checkView);

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
		this.parentDictId = id;
		this.refreshListAdapter();
	}

	private void export(long[] dictIds) {
		Long[] ids = new Long[dictIds.length];
		for (int i = 0; i < dictIds.length; i++) {
			ids[i] = dictIds[i];
		}
		this.exportTask = new ExportTask(this);
		this.exportTask.execute(ids);
	}
	
	/**
	 * Move to be a child of another dictionary. The parent will be selected in this method.
	 * @param id
	 */
	private void moveUnderDictionary(long movedDictionary, long parentDictionary) {
		try {
			DictionaryDS.setAsChildOf(this.db, movedDictionary, parentDictionary);
			Toast.makeText(this, R.string.dict_list_dict_moved_toast, Toast.LENGTH_LONG).show();
		} catch (ParentIsTheSameException ex) {
			Toast.makeText(this, R.string.dict_list_err_parent_is_same, Toast.LENGTH_LONG).show();
		} catch (ParentIsDescendantException ex) {
			Toast.makeText(this, R.string.dict_list_err_parent_is_descendant, Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Move selected dictionary to be a root dictionary
	 * @param id
	 */
	private void moveToRoot(long id) {
		DictionaryDS.setAsRoot(this.db, id);
		this.refreshListAdapter();
		Toast.makeText(this, R.string.dict_list_dict_moved_root_toast, Toast.LENGTH_LONG).show();
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	/**
	 * Listener which is called when user click on back arrow to go to parent dictionary.
	 */
	private OnClickListener goUpClickListener = new OnClickListener() {

		public void onClick(View v) {
			Cursor pDict = DictionaryDS.getParentDictionary(db, parentDictId);
			pDict.moveToFirst();
			if (pDict.isAfterLast()) {
				parentDictId = null;
			} else {
				parentDictId = pDict.getLong(pDict.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_ID));
			}
			pDict.close();
			refreshListAdapter();
		}
	};

	private class DeleteYesListener implements DialogInterface.OnClickListener {

		private long dictId;
		private CheckBox delDescendantsCheck;

		public DeleteYesListener(long dictId, CheckBox delDescendantsCheck) {
			super();
			this.dictId = dictId;
			this.delDescendantsCheck = delDescendantsCheck;
		}

		public void onClick(DialogInterface dialog, int which) {
			DictionaryDS.deleteDict(db, dictId, this.delDescendantsCheck.isChecked());

			Toast.makeText(DictListActivity.this,
					R.string.dict_list_dict_deleted_toast, Toast.LENGTH_SHORT)
					.show();
			DictListActivity.this.refreshListAdapter();
		}

	}

	private class DictListAdapter extends CursorAdapter {

		public DictListAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			DictListViewHolder h = (DictListViewHolder) view.getTag();

			Language natLang = Language.getById(cursor.getInt(cursor.getColumnIndex(
					VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG)));
			h.nativeFlag.setImageResource(natLang.getIconId());

			Language forLang = Language.getById(cursor.getInt(cursor.getColumnIndex(
					VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG)));
			h.foreignFlag.setImageResource(forLang.getIconId());

			String dictText = cursor.getString(cursor.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_NAME));
			h.dictText.setText(dictText);

			h.goInto.setTag(cursor.getLong(cursor.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_ID)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.inf_dictionary_item, null);

			DictListViewHolder holder = new DictListViewHolder();
			holder.nativeFlag = (ImageView) view.findViewById(R.id.nativeLangIcon);
			holder.foreignFlag = (ImageView) view.findViewById(R.id.foreignLangIcon);
			holder.dictText = (TextView) view.findViewById(R.id.languageText);
			holder.goInto = view.findViewById(R.id.goInto);

			view.setTag(holder);

			holder.goInto.setOnClickListener(this.goIntoClickListener);

			return view;
		}

		private OnClickListener goIntoClickListener = new OnClickListener() {

			public void onClick(View v) {
				Long id = (Long) v.getTag();
				showDescendants(id);
			}
		};

		private class DictListViewHolder {
			public ImageView nativeFlag;
			public ImageView foreignFlag;
			public TextView dictText;
			public View goInto;
		}

	}
}
