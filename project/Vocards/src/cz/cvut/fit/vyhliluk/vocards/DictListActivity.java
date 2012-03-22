package cz.cvut.fit.vyhliluk.vocards;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

/**
 * Activity class
 * 
 * @author Lucky
 * 
 */
public class DictListActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_DICT = 1;

	// ================= INSTANCE ATTRIBUTES ====================

	private ListView dictList = null;
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
	protected void onResume() {
		super.onResume();

		this.refreshListAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, res.getString(R.string.dict_list_menu_show_filter));
		menu.add(none, MENU_NEW_DICT, none, res.getString(R.string.dict_list_menu_new_dict));
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
		}

		return super.onOptionsItemSelected(item);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	/**
	 * Activity initialization
	 */
	private void init() {
		this.dictList = (ListView) findViewById(R.id.dictList);
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);

		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(
				this,
				R.layout.inf_dictionary_item,
				null,
				new String[] {
						VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_NAME
				},
				new int[] {
						R.id.nativeLangIcon,
						R.id.foreignLangIcon,
						R.id.languageText
				});
		listAdapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				String colName = cursor.getColumnName(columnIndex);
				if (VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG.equals(colName)
						|| VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG.equals(colName)) {
					ImageView img = (ImageView) view;
					Language lang = Language.getById(cursor.getInt(columnIndex));
					img.setImageResource(lang.getIconId());
					return true;
				} else {
					return false;
				}
			}
		});
		this.dictList.setAdapter(listAdapter);
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
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.dictList.getAdapter();
		Cursor c = this.db.query(VocardsDataSource.DICTIONARY_TABLE,
				new String[] {
						VocardsDataSource.DICTIONARY_COLUMN_ID,
						VocardsDataSource.DICTIONARY_COLUMN_NAME,
						VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG },
				null,
				null,
				VocardsDataSource.DICTIONARY_COLUMN_NAME);
		adapter.changeCursor(c);
	}

	private void addDictActivity() {
		Intent i = new Intent(this, DictAddActivity.class);
		startActivity(i);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================
}
