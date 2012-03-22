package cz.cvut.fit.vyhliluk.vocards;

import java.util.Arrays;
import java.util.List;

import cz.cvut.fit.vyhliluk.vocards.adapter.LanguageAdapter;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
			String name = nameEdit.getText().toString();
			Language nativeLang = (Language)nativeSpinner.getSelectedItem();
			Language foreignLang = (Language)foreignSpinner.getSelectedItem();
			
			saveDictionary(name, nativeLang, foreignLang);
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

		List<Language> langs = Arrays.asList(Language.values());
		ArrayAdapter<Language> langAdapter = new LanguageAdapter(
				this, 
				R.layout.inf_language_item, 
				langs);
		this.nativeSpinner.setAdapter(langAdapter);
		this.foreignSpinner.setAdapter(langAdapter);

		this.addButton.setOnClickListener(this.addButtonClickListener);
	}
	
	private void saveDictionary(String name, Language nativeLang, Language foreignLang) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NAME, name);
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG, foreignLang.getId());
		this.db.insert(VocardsDataSource.DICTIONARY_TABLE, val);
		
		Toast.makeText(this, res.getString(R.string.add_dict_created_toast), Toast.LENGTH_SHORT).show();
		
		Intent i = new Intent(this, DictListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
