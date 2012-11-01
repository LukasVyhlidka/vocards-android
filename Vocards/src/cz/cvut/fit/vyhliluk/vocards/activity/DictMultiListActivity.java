package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

public class DictMultiListActivity extends AbstractListActivity {
	// ================= STATIC ATTRIBUTES ======================
	
	public static final int REQUEST_DICT_LIST = 0;
	public static final String KEY_RESULT_LIST = "result_list";

	// ================= INSTANCE ATTRIBUTES ====================

	private Button okBtn = null;
	private Button cancelBtn = null;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_list_multi);

		this.init();
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

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.okBtn = (Button) findViewById(R.id.okButton);
		this.cancelBtn = (Button) findViewById(R.id.cancelButton);

		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(
				this,
				android.R.layout.simple_list_item_multiple_choice,
				null,
				new String[] {
						VocardsDS.DICT_COL_NAME
				},
				new int[] {
						android.R.id.text1
				});

		this.setListAdapter(listAdapter);
		
		this.okBtn.setOnClickListener(okClickListener);
		this.cancelBtn.setOnClickListener(cancelClickListener);
	}

	private void refreshListAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		DBUtil.closeExistingCursor(adapter.getCursor());
		Cursor c = DictionaryDS.getDictionaries(this.db);
		adapter.changeCursor(c);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================
	
	OnClickListener okClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			long[] checkedIds = getListView().getCheckItemIds();
			Intent i = getIntent();
			i.putExtra(KEY_RESULT_LIST, checkedIds);
			
			setResult(RESULT_OK, i);
			finish();
		}
	};
	
	OnClickListener cancelClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
	};

}
