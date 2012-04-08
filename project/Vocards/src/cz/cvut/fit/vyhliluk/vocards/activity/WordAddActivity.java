package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.StringUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.WordDS;

public class WordAddActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	public static final String EXTRAS_NATIVE_WORD = "native";
	public static final String EXTRAS_CARD_ID = "cardId";

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText nativeEdit = null;
	private EditText foreignEdit = null;

	private long cardId = -1;
	private long dictId = -1;

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

		this.dictId = Settings.getActiveDictionaryId();

		Intent i = this.getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			this.handleBundle(b);
		}
	}

	private void handleBundle(Bundle b) {
		if (b.containsKey(EXTRAS_NATIVE_WORD)) {
			String natWord = b.getString(EXTRAS_NATIVE_WORD);
			this.nativeEdit.setText(natWord);
		} else if (b.containsKey(EXTRAS_CARD_ID)) {
			this.cardId = b.getLong(EXTRAS_CARD_ID);
			Cursor c = WordDS.getWordById(db, dictId, cardId);
			c.moveToFirst();
			this.nativeEdit.setText(c.getString(c.getColumnIndex(WordDS.NATIVE_WORD)));
			this.foreignEdit.setText(c.getString(c.getColumnIndex(WordDS.FOREIGN_WORD)));
		}
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	OnClickListener createClickListener = new OnClickListener() {

		public void onClick(View v) {
			String natWord = nativeEdit.getText().toString();
			String forWord = foreignEdit.getText().toString();

			if (StringUtil.isEmpty(natWord) || StringUtil.isEmpty(forWord)) {
				Toast.makeText(WordAddActivity.this, R.string.add_word_empty_word_toast, Toast.LENGTH_SHORT);
				return;
			}

			if (cardId == -1) {
				WordDS.createCard(db, natWord, forWord, dictId);
			} else {
				WordDS.updateCard(db, cardId, natWord, forWord);
			}

			Intent i = new Intent(WordAddActivity.this, WordListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
	};

}
