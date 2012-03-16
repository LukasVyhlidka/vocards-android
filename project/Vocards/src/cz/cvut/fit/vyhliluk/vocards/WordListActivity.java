package cz.cvut.fit.vyhliluk.vocards;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class WordListActivity extends AbstractActivity {

	// ================= STATIC ATTRIBUTES ======================
	
	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_WORD = 1;

	// ================= INSTANCE ATTRIBUTES ====================
	
	private ListView wordList = null;
	private EditText filterEdit = null;
	
	private MenuItem menuFilter = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.word_list);
		
		this.init();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;
		
		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, res.getString(R.string.word_list_menu_show_filter));
		menu.add(none, MENU_NEW_WORD, none, res.getString(R.string.word_list_new_word_hint));
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SHOW_HIDE_FILTER:
				this.showHideFilter();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.wordList = (ListView)findViewById(R.id.wordList);
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);
		
		String[] values = new String[] { "Item 1", "Item 2", "Item 3",
				"Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9",
				"Item 10", "Item 11", "Item 12", "Item 13", "Item 14",
				"Item 15", "Item 16", "Item 17", "Item 18", "Item 19",
				"Item 20" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		
		this.wordList.setAdapter(adapter);
	}
	
	/**
	 * Hides or Show a filter
	 */
	private void showHideFilter() {
		Resources res = this.getResources();
		
		if (this.filterEdit.getVisibility() == EditText.GONE) {
			this.filterEdit.setVisibility(EditText.VISIBLE);
			this.menuFilter.setTitle(res.getString(R.string.word_list_menu_hide_filter));
		} else {
			this.filterEdit.setVisibility(EditText.GONE);
			this.menuFilter.setTitle(res.getString(R.string.word_list_menu_show_filter));
		}
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
