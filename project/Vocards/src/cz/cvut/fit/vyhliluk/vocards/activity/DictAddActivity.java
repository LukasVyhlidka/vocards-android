package cz.cvut.fit.vyhliluk.vocards.activity;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.adapter.LanguageAdapter;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.util.StringUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

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
			
			if (StringUtil.isEmpty(name)) {
				Toast.makeText(DictAddActivity.this, R.string.add_dict_empty_name_toast, Toast.LENGTH_SHORT);
				return;
			}
			
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
		DictionaryDS.createDictionary(this.db, name, nativeLang, foreignLang);
		
		Toast.makeText(this, res.getString(R.string.add_dict_created_toast), Toast.LENGTH_SHORT).show();
		
		Intent i = new Intent(this, DictListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
