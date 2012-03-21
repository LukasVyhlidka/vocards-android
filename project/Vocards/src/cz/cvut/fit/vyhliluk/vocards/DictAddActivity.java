package cz.cvut.fit.vyhliluk.vocards;

import java.util.Arrays;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class DictAddActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText nameEdit = null;
	private Spinner nativeSpinner = null;
	private Spinner foreignSpinner = null;
	private Button addButton = null;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_add);

		this.init();
	}

	// ================= INSTANCE METHODS =======================

	// ================= VIEW HANDLERS ==========================

	OnClickListener addButtonClickListener = new OnClickListener() {

		public void onClick(View v) {

		}
	};

	// ================= PRIVATE METHODS ========================

	/**
	 * Activity initialization
	 */
	private void init() {
		this.nameEdit = (EditText) findViewById(R.id.nameEdit);
		this.nativeSpinner = (Spinner) findViewById(R.id.nativeSpinner);
		this.foreignSpinner = (Spinner) findViewById(R.id.foreignSpinner);
		this.addButton = (Button) findViewById(R.id.buttonAdd);

		ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(
				this, 
				android.R.layout.simple_spinner_item, 
				Arrays.asList("X", "Y", "Z"));
		this.nativeSpinner.setAdapter(langAdapter);
		this.foreignSpinner.setAdapter(langAdapter);

		this.addButton.setOnClickListener(this.addButtonClickListener);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
