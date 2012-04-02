package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.WordDS;

public class WordListActivity extends AbstractListActivity {

	// ================= STATIC ATTRIBUTES ======================
	
	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_WORD = 1;

	// ================= INSTANCE ATTRIBUTES ====================
	
	private EditText filterEdit = null;
	
	private MenuItem menuFilter = null;
	
	private long selectedDictId;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;
		
		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, res.getString(R.string.word_list_menu_show_filter));
		menu.add(none, MENU_NEW_WORD, none, res.getString(R.string.word_list_new_word));
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SHOW_HIDE_FILTER:
				this.showHideFilter();
				break;
			case MENU_NEW_WORD:
				this.createWord();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);
		
		this.selectedDictId = Settings.getActiveDictionaryId();
		
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(
				this,
				R.layout.inf_word_item,
				null,
				new String[] {
						WordDS.NATIVE_WORD,
						WordDS.FOREIGN_WORD
				},
				new int[] {
						R.id.nativeWord,
						R.id.foreignWord
				});

		this.setListAdapter(listAdapter);
	}
	
	private void refreshListAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		
		Cursor c = WordDS.getWordsByDictId(this.db, this.selectedDictId);
		adapter.changeCursor(c);
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
	
	private void createWord() {
		Intent i = new Intent(this, WordAddActivity.class);
		startActivity(i);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
