package cz.cvut.fit.vyhliluk.vocards.abstr;

import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

public abstract class AbstractActivity extends Activity {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	protected Resources res = null;
	protected VocardsDataSource db = null;

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

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void initDb() {
		if (this.db == null) {
			this.db = new VocardsDataSource(this);
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
