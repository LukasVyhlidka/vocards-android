package cz.cvut.fit.vyhliluk.vocards.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.ImportTask;
import cz.cvut.fit.vyhliluk.vocards.util.StorageUtil;

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
			try {
				String scheme = i.getScheme();
				if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
					// handle as content uri
					InputStream is = getContentResolver().openInputStream(i.getData());
					String content = StorageUtil.readStream(is);

					this.importTask = new ImportTask(this);
					this.importTask.execute(content);
				} else {
					i.getData();
					File f = new File(i.getData().getPath());
					String content = StorageUtil.readEntireFile(f);

					this.importTask = new ImportTask(this);
					this.importTask.execute(content);
				}
			} catch (IOException ex) {
				Toast.makeText(this, "Import Error", Toast.LENGTH_LONG);
			}

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
