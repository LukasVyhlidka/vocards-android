package cz.cvut.fit.vyhliluk.vocards.activity;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.ImportTask;

public class ImportActivity extends AbstractActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Intent i = getIntent();
		i.getData();
		File f = new File(i.getData().getPath());
		ImportTask it = new ImportTask(this);
		it.execute(f);
	}
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================

	//================= STATIC METHODS =========================

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
