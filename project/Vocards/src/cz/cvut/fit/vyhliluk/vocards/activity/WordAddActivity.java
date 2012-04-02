package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.WordDS;

public class WordAddActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText nativeEdit = null;
	private EditText foreignEdit = null;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.word_add);
		
		this.init();
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.nativeEdit = (EditText) findViewById(R.id.nativeEdit);
		this.foreignEdit = (EditText) findViewById(R.id.foreignEdit);
		
		Button createBtn = (Button) findViewById(R.id.buttonAdd);
		createBtn.setOnClickListener(this.createClickListener);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================
	
	OnClickListener createClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			String natWord = nativeEdit.getText().toString();
			String forWord = foreignEdit.getText().toString();
			long dictId = Settings.getActiveDictionaryId();
			
			WordDS.createCard(db, natWord, forWord, dictId);
			
			Intent i = new Intent(WordAddActivity.this, WordListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
	};

}
