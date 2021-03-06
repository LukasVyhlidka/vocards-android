package cz.cvut.fit.vyhliluk.vocards.activity;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.StringUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

public class DictAddActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	public static final String EXTRAS_DICT_ID = "dictId";
	public static final String EXTRAS_PARENT_DICT_ID = "parentDictId";

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText nameEdit = null;
	private Spinner nativeSpinner = null;
	private Spinner foreignSpinner = null;
	private Button addButton = null;

	private Long dictId = null;
	private Long parentDictId = null;

	private ArrayAdapter<Language> langAdapter;

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
			Language nativeLang = (Language) nativeSpinner.getSelectedItem();
			Language foreignLang = (Language) foreignSpinner.getSelectedItem();

			if (StringUtil.isEmpty(name)) {
				Toast.makeText(DictAddActivity.this, R.string.add_dict_empty_name_toast, Toast.LENGTH_SHORT).show();
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
		this.langAdapter = new LanguageAdapter(
				this,
				R.layout.inf_language_item,
				langs);
		this.nativeSpinner.setAdapter(langAdapter);
		this.foreignSpinner.setAdapter(langAdapter);

		this.addButton.setOnClickListener(this.addButtonClickListener);

		Intent i = this.getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			this.handleBundle(b);
		}
	}

	private void handleBundle(Bundle b) {
		if (b.containsKey(EXTRAS_DICT_ID)) {
			this.dictId = b.getLong(EXTRAS_DICT_ID);

			Cursor c = DictionaryDS.getById(this.db, this.dictId);
			c.moveToFirst();
			this.nameEdit.setText(c.getString(c.getColumnIndex(VocardsDS.DICT_COL_NAME)));

			int natLang = c.getInt(c.getColumnIndex(VocardsDS.DICT_COL_NATIVE_LANG));
			int natLangPos = this.langAdapter.getPosition(Language.getById(natLang));
			this.nativeSpinner.setSelection(natLangPos);

			int forLang = c.getInt(c.getColumnIndex(VocardsDS.DICT_COL_FOREIGN_LANG));
			int forLangPos = this.langAdapter.getPosition(Language.getById(forLang));
			this.foreignSpinner.setSelection(forLangPos);
			
			c.close();
			
			this.addButton.setText(getString(R.string.add_dict_edit));
		}
		
		if (b.containsKey(EXTRAS_PARENT_DICT_ID)) {
			this.parentDictId = b.getLong(EXTRAS_PARENT_DICT_ID);
		}
	}

	private void saveDictionary(String name, Language nativeLang, Language foreignLang) {
		if (this.dictId == null) {
			Log.d("saving dict", "root id = "+ this.parentDictId);
			DictionaryDS.createDictionary(this.db, name, nativeLang, foreignLang, this.parentDictId);
			Toast.makeText(this, res.getString(R.string.add_dict_created_toast), Toast.LENGTH_SHORT).show();
		} else {
			DictionaryDS.updateDictionary(this.db, this.dictId, name, nativeLang, foreignLang);
			Toast.makeText(this, res.getString(R.string.add_dict_edited_toast), Toast.LENGTH_SHORT).show();
		}

//		Intent i = new Intent(this, DictListActivity.class);
//		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(i);
		finish();
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
