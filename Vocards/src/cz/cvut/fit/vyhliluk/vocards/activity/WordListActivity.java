package cz.cvut.fit.vyhliluk.vocards.activity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
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
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
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
	public static final int MENU_SELECTION_CHANGE = 3;
	public static final int MENU_MOVE_SELECTED = 4;

	public static final int CTX_DELETE_WORD = 0;
	public static final int CTX_EDIT_WORD = 1;
	public static final int CTX_MOVE_WORD = 2;

	private static final int REQ_PARENT_DICT = 0;

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText filterEdit = null;
	private EditText newWordEdit = null;

	private TextView emptyText = null;

	private Button addWordBtn = null;

	private MenuItem menuFilter = null;

	private long selectedDictId;

	private String orderBy = null;
	private AlertDialog alertDialog = null;

	/**
	 * This is used for storing the moved word Ids when user selects the parent
	 * dictionary
	 */
	private Collection<Long> movedWordIds = null;

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
		this.emptyText.setText("");
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

		WordListAdapter adapter = (WordListAdapter) this.getListAdapter();
		adapter.getCursor().close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		this.menuFilter = menu.add(none, MENU_SHOW_HIDE_FILTER, none, res.getString(R.string.word_list_menu_show_filter));
		this.menuFilter.setIcon(R.drawable.icon_filter);

		menu.add(none, MENU_NEW_WORD, none, R.string.word_list_menu_new_word).setIcon(R.drawable.icon_new);
		menu.add(none, MENU_ORDER, none, R.string.word_list_menu_order).setIcon(R.drawable.icon_sort);
		menu.add(none, MENU_SELECTION_CHANGE, none, R.string.word_list_menu_selection_change).setIcon(R.drawable.icon_list);
		menu.add(none, MENU_MOVE_SELECTED, none, R.string.word_list_menu_move_selected).setIcon(R.drawable.icon_move);

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
			case MENU_SELECTION_CHANGE:
				this.getActualAdapter().changeSelectionMode();
				break;
			case MENU_MOVE_SELECTED:
				this.moveCards(this.getActualAdapter().getSelectedIds());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem moveSel = menu.findItem(MENU_MOVE_SELECTED);
		moveSel.setVisible(this.getActualAdapter().isMulti());

		return super.onPrepareOptionsMenu(menu);
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

	@Override
	public Object onRetainNonConfigurationInstance() {
		boolean multi = this.getActualAdapter().multi;
		Set<Long> selectedIds = this.getActualAdapter().selectedIds;
		return new RetainConfigurationCrate(multi, selectedIds);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.filterEdit = (EditText) findViewById(R.id.filterEdit);
		this.newWordEdit = (EditText) findViewById(R.id.editNewWord);
		this.addWordBtn = (Button) findViewById(R.id.buttonAdd);
		this.emptyText = (TextView) findViewById(android.R.id.empty);

		this.selectedDictId = Settings.getActiveDictionaryId();
		this.orderBy = Settings.getWordOrdering();

		WordListAdapter listAdapter = new WordListAdapter(this, null);

		this.filterEdit.addTextChangedListener(this.filterEditWatcher);

		listAdapter.setFilterQueryProvider(this.listFilterProvider);
		this.setListAdapter(listAdapter);
		this.registerForContextMenu(this.getListView());

		this.addWordBtn.setOnClickListener(this.addWordClickListener);
		
		RetainConfigurationCrate crate = (RetainConfigurationCrate) this.getLastNonConfigurationInstance();
		if (crate != null) {
			listAdapter.multi = crate.multi;
			listAdapter.selectedIds = crate.selectedIds;
		}
	}

	private void refreshListAdapter() {
		WordListAdapter adapter = (WordListAdapter) this.getListAdapter();
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

	private void moveCards(Collection<Long> wordIds) {
		this.movedWordIds = wordIds;

		Toast.makeText(this, R.string.word_list_select_parent_title, Toast.LENGTH_LONG).show();

		Intent i = new Intent(this, DictListActivity.class);
		i.putExtra(DictListActivity.EXTRAS_ONLY_DICT_SELECTION, true);
		i.putExtra(DictListActivity.EXTRAS_MESSAGE, getString(R.string.word_list_select_parent_title));
		startActivityForResult(i, REQ_PARENT_DICT);
	}

	private void moveCards(Collection<Long> wordIds, long parentDictId) {
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
				Settings.setWordOrdering(orderBy);
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

	private WordListAdapter getActualAdapter() {
		WordListAdapter adapter = (WordListAdapter) this.getListAdapter();
		return adapter;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

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
			WordListAdapter a = getActualAdapter();
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

	private class WordListAdapter extends CursorAdapter {

		private boolean multi = false;
		private Set<Long> selectedIds = new HashSet<Long>();

		public WordListAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void changeSelectionMode() {
			this.multi = !this.multi;
			this.selectedIds.clear();
			if (this.multi) {
				getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				refreshListAdapter();
			} else {
				getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
				refreshListAdapter();
			}
		}

		public boolean isMulti() {
			return this.multi;
		}

		public Collection<Long> getSelectedIds() {
			return new LinkedList<Long>(this.selectedIds);
		}

		@Override
		public void bindView(View view, Context ctx, Cursor c) {
			WordListViewHolder holder = (WordListViewHolder) view.getTag();

			long id = c.getLong(c.getColumnIndex(VocardsDS.CARD_COL_ID));

			holder.checkbox.setVisibility(multi ? View.VISIBLE : View.GONE);
			holder.natWord.setText(c.getString(c.getColumnIndex(VocardsDS.CARD_COL_NATIVE)));
			holder.forWord.setText(c.getString(c.getColumnIndex(VocardsDS.CARD_COL_FOREIGN)));

			int factor = c.getInt(c.getColumnIndex(VocardsDS.CARD_COL_FACTOR));
			holder.factor.setText(CardUtil.cardFactorPercent(factor));

			holder.checkbox.setTag(id);
			holder.checkbox.setChecked(this.selectedIds.contains(id));
		}

		@Override
		public View newView(Context ctx, Cursor c, ViewGroup group) {
			View view = View.inflate(ctx, R.layout.inf_word_item, null);

			WordListViewHolder holder = new WordListViewHolder();
			holder.checkbox = (CheckBox) view.findViewById(R.id.checkbox);
			holder.natWord = (TextView) view.findViewById(R.id.nativeWord);
			holder.forWord = (TextView) view.findViewById(R.id.foreignWord);
			holder.factor = (TextView) view.findViewById(R.id.factor);

			view.setTag(holder);
			holder.checkbox.setOnCheckedChangeListener(this.checkboxSelChangeListener);
			holder.checkbox.setOnClickListener(this.checkboxClickListener);

			return view;
		}

		private class WordListViewHolder {
			public CheckBox checkbox;
			public TextView natWord;
			public TextView forWord;
			public TextView factor;
		}

		private OnClickListener checkboxClickListener = new OnClickListener() {

			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				long id = (Long) cb.getTag();
				if (cb.isChecked()) {
					selectedIds.add(id);
				} else {
					selectedIds.remove(id);
				}
			}
		};

		private OnCheckedChangeListener checkboxSelChangeListener = new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Log.d("onCheckedChanged", isChecked+"");
				// long id = (Long) buttonView.getTag();
				// if (isChecked) {
				// selectedIds.add(id);
				// } else {
				// selectedIds.remove(id);
				// }
			}
		};

	}

	private static class RetainConfigurationCrate {
		public boolean multi;
		public Set<Long> selectedIds = null;

		public RetainConfigurationCrate(boolean multi, Set<Long> selectedIds) {
			super();
			this.multi = multi;
			this.selectedIds = selectedIds;
		}

	}

}
