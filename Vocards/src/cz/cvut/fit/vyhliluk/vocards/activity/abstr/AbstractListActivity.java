package cz.cvut.fit.vyhliluk.vocards.activity.abstr;

import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

public class AbstractListActivity extends ListActivity {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	protected Resources res = null;
	protected VocardsDS db = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.res = this.getResources();
		this.initDb();
	}

	@Override
	protected void onPause() {
		super.onPause();

		this.closeDb();
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.initDb();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		this.initDb();
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void initDb() {
		if (this.db == null) {
			this.db = new VocardsDS(this);
			this.db.open();
		}
	}

	private void closeDb() {
		if (this.db != null) {
			this.db.close();
			this.db = null;
		}
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
