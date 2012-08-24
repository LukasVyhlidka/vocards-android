package cz.cvut.fit.vyhliluk.vocards.activity;

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.StringUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.WordDS;

public class WordListActivity extends AbstractListActivity {

	// ================= STATIC ATTRIBUTES ======================

	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_WORD = 1;
	public static final int MENU_ORDER = 2;

	public static final int CTX_DELETE_WORD = 0;
	public static final int CTX_EDIT_WORD = 1;
	public static final int CTX_MOVE_WORD = 2;

	private static final int REQ_PARENT_DICT = 0;

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText filterEdit = null;
	private EditText newWordEdit = null;

	private Button addWordBtn = null;

	private MenuItem menuFilter = null;

	private long selectedDictId;

	private String orderBy = null;
	private AlertDialog alertDialog = null;

	/**
	 * This is used for storing the moved word Ids when user selects the parent
	 * dictionary
	 */
	private List<Long> movedWordIds = null;

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
	protected void onPause() {
		super.onPause();

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		adapter.getCursor().close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, res.getString(R.string.word_list_menu_show_filter));
		this.menuFilter.setIcon(R.drawable.icon_filter);

		menu.add(none, MENU_NEW_WORD, none, res.getString(R.string.word_list_menu_new_word)).setIcon(R.drawable.icon_new);
		menu.add(none, MENU_ORDER, none, res.getString(R.string.word_list_menu_order)).setIcon(R.drawable.icon_sort);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SHOW_HIDE_FILTER:
				this.showHideFilter();
				break;
			case MENU_NEW_WORD:
				this.createWord(null);
				break;
			case MENU_ORDER:
				this.showSelectOrderDialog();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		int none = Menu.NONE;
		menu.add(none, CTX_EDIT_WORD, none, R.string.word_list_ctx_edit);
		menu.add(none, CTX_DELETE_WORD, none, R.string.word_list_ctx_delete);
		menu.add(none, CTX_MOVE_WORD, none, R.string.word_list_ctx_move);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case CTX_DELETE_WORD:
				this.deleteCard(info.id);
				break;
			case CTX_EDIT_WORD:
				this.editCard(info.id);
				break;
			case CTX_MOVE_WORD:
				this.moveCards(Arrays.asList(info.id));
				break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQ_PARENT_DICT) {
			if (resultCode == RESULT_OK) {
				long dictId = data.getExtras().getLong(DictListActivity.KEY_RESULT_DICT_ID);
				this.moveCards(this.movedWordIds, dictId);
			}
		}
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);
		this.newWordEdit = (EditText) findViewById(R.id.editNewWord);
		this.addWordBtn = (Button) findViewById(R.id.buttonAdd);

		this.selectedDictId = Settings.getActiveDictionaryId();

		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(
				this,
				R.layout.inf_word_item,
				null,
				new String[] {
						WordDS.NATIVE_WORD,
						WordDS.FOREIGN_WORD,
						VocardsDS.CARD_COL_FACTOR
				},
				new int[] {
						R.id.nativeWord,
						R.id.foreignWord,
						R.id.factor
				});

		this.filterEdit.addTextChangedListener(this.filterEditWatcher);

		listAdapter.setViewBinder(this.wordListBinder);
		listAdapter.setFilterQueryProvider(this.listFilterProvider);
		this.setListAdapter(listAdapter);
		this.registerForContextMenu(this.getListView());

		this.addWordBtn.setOnClickListener(this.addWordClickListener);
	}

	private void refreshListAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		DBUtil.closeExistingCursor(adapter.getCursor());

		Cursor c = null;
		if (this.orderBy != null) {
			c = WordDS.getOrdWordsByDictId(this.db, this.selectedDictId, this.orderBy);
		} else {
			c = WordDS.getWordsByDictId(this.db, this.selectedDictId);
		}
		adapter.changeCursor(c);
	}

	private void deleteCard(long id) {
		WordDS.removeCard(this.db, id);

		Toast.makeText(this, R.string.word_list_deleted_toast, Toast.LENGTH_SHORT).show();
		this.refreshListAdapter();
	}

	private void editCard(long id) {
		Intent i = new Intent(this, WordAddActivity.class);
		i.putExtra(WordAddActivity.EXTRAS_CARD_ID, id);
		startActivity(i);
	}

	private void moveCards(List<Long> wordIds) {
		this.movedWordIds = wordIds;

		Intent i = new Intent(this, DictListActivity.class);
		i.putExtra(DictListActivity.EXTRAS_ONLY_DICT_SELECTION, true);
		i.putExtra(DictListActivity.EXTRAS_MESSAGE, getString(R.string.word_list_select_parent_title));
		startActivityForResult(i, REQ_PARENT_DICT);
	}

	private void moveCards(List<Long> wordIds, long parentDictId) {
		WordDS.moveCards(db, wordIds, parentDictId);
		Toast.makeText(this, R.string.word_list_word_moved_toast, Toast.LENGTH_LONG).show();
		this.refreshListAdapter();
	}

	/**
	 * Hides or Show a filter
	 */
	private void showHideFilter() {
		Resources res = this.getResources();

		if (this.filterEdit.getVisibility() == EditText.GONE) {
			this.filterEdit.setVisibility(EditText.VISIBLE);
			this.filterEdit.requestFocus();
			this.menuFilter.setTitle(res.getString(R.string.word_list_menu_hide_filter));
		} else {
			this.filterEdit.setVisibility(EditText.GONE);
			this.filterEdit.setText("");
			this.menuFilter.setTitle(res.getString(R.string.word_list_menu_show_filter));
		}
	}

	private void showSelectOrderDialog() {
		String[] labels = res.getStringArray(R.array.word_list_ordering_labels);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.word_list_select_order_dialog_title);
		builder.setItems(labels, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				alertDialog = null;
				String[] values = res.getStringArray(R.array.word_list_ordering_values);
				orderBy = values[item];
				refreshListAdapter();
			}
		});
		alertDialog = builder.create();
		alertDialog.show();
	}

	private void createWord(String natWord) {
		Intent i = new Intent(this, WordAddActivity.class);
		if (natWord != null) {
			i.putExtra(WordAddActivity.EXTRAS_NATIVE_WORD, natWord);
		}
		startActivity(i);
	}

	private SimpleCursorAdapter getActualAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		return adapter;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	ViewBinder wordListBinder = new ViewBinder() {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			String colName = cursor.getColumnName(columnIndex);
			if (VocardsDS.CARD_COL_FACTOR.equals(colName)) {
				TextView v = (TextView) view;
				int factor = cursor.getInt(columnIndex);
				v.setText(CardUtil.cardFactorPercent(factor));
				return true;
			}
			return false;
		}
	};

	OnClickListener addWordClickListener = new OnClickListener() {

		public void onClick(View v) {
			String natWord = newWordEdit.getText().toString();
			if (StringUtil.isEmpty(natWord)) {
				Toast.makeText(WordListActivity.this, R.string.word_list_empty_word_toast, Toast.LENGTH_SHORT).show();
				return;
			}

			createWord(natWord);
		}
	};

	private TextWatcher filterEditWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			SimpleCursorAdapter a = getActualAdapter();
			a.getFilter().filter(s);
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub

		}

		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}
	};

	FilterQueryProvider listFilterProvider = new FilterQueryProvider() {

		public Cursor runQuery(CharSequence constraint) {
			return WordDS.getWordsByDictIdFilter(db, selectedDictId, constraint.toString());
		}
	};

}
