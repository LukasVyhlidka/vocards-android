package cz.cvut.fit.vyhliluk.vocards.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractListActivity;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DictUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

public class DictMultiListActivity extends AbstractListActivity {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================
	
	private Button okBtn = null;
	private Button cancelBtn = null;

	//================= STATIC METHODS =========================

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================
	
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

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================
	
	private void init() {
		this.okBtn = (Button) findViewById(R.id.okButton);
		this.cancelBtn = (Button) findViewById(R.id.cancelButton);

		this.setListAdapter(DictUtil.createDictListAdapter(this));
	}
	
	private void refreshListAdapter() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.getListAdapter();
		DBUtil.closeExistingCursor(adapter.getCursor());
		Cursor c = DictionaryDS.getDictionaries(this.db);
		adapter.changeCursor(c);
	}

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
