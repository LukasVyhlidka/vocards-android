package cz.cvut.fit.vyhliluk.vocards.activity;

import java.io.File;

import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.ImportTask;

public class ImportActivity extends AbstractActivity {

	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private ImportTask importTask = null;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Object o = getLastNonConfigurationInstance();
		if (o != null) {
			this.importTask = (ImportTask) o;
			this.importTask.attach(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent i = getIntent();
		if (i != null) {
			i.getData();
			File f = new File(i.getData().getPath());

			this.importTask = new ImportTask(this);
			this.importTask.execute(f);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (this.importTask == null || this.importTask.getStatus().equals(Status.FINISHED)) {
			return super.onRetainNonConfigurationInstance();
		}
		
		this.importTask.detach();
		return this.importTask;
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
