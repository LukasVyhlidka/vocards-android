package cz.cvut.fit.vyhliluk.vocards;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, res.getString(R.string.dict_list_menu_show_filter));
		menu.add(none, MENU_NEW_DICT, none, res.getString(R.string.dict_list_menu_new_dict));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onPrepareOptionsMenu(menu);
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

		String[] values = new String[] { "Dict 1", "Dict 2", "Dict 3",
				"Dict 4", "Dict 5", "Dict 6", "Dict 7", "Dict 8", "Dict 9",
				"Dict 10", "Dict 11", "Dict 12", "Dict 13", "Dict 14",
				"Dict 15", "Dict 16", "Dict 17", "Dict 18", "Dict 19",
				"Dict 20" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);

		this.dictList.setAdapter(adapter);
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
	
	private void addDictActivity() {
		Intent i = new Intent(this, DictAddActivity.class);
		startActivity(i);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================
}
