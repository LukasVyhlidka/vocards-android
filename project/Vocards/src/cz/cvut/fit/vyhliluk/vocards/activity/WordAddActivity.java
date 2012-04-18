package cz.cvut.fit.vyhliluk.vocards.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
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

	private LinearLayout nativeContainer = null;
	private LinearLayout foreignContainer = null;

	private List<EditText> nativeEdits = new ArrayList<EditText>();
	private List<EditText> foreignEdits = new ArrayList<EditText>();

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
		this.nativeContainer = (LinearLayout) findViewById(R.id.nativeContainer);
		this.foreignContainer = (LinearLayout) findViewById(R.id.foreignContainer);
		
		this.nativeEdits.add(this.nativeEdit);
		this.foreignEdits.add(this.foreignEdit);

		Button createBtn = (Button) findViewById(R.id.buttonAdd);
		createBtn.setOnClickListener(this.createClickListener);

		ImageView nativeAdd = (ImageView) findViewById(R.id.nativeAdd);
		nativeAdd.setOnClickListener(this.nativeAddListener);
		
		ImageView foreignAdd = (ImageView) findViewById(R.id.foreignAdd);
		foreignAdd.setOnClickListener(this.foreignAddListener);

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
			this.foreignEdit.requestFocus();
		} else if (b.containsKey(EXTRAS_CARD_ID)) {
			this.cardId = b.getLong(EXTRAS_CARD_ID);
			
			Cursor nat = WordDS.getCardNativeWords(db, this.cardId);
			this.loadNativeWords(nat);
			nat.close();
			
			Cursor foreign = WordDS.getCardForeignWords(db, this.cardId);
			this.loadForeignWords(foreign);
			foreign.close();
		}
	}
	
	private void loadNativeWords(Cursor c) {
		c.moveToFirst();
		this.nativeEdit.setText(c.getString(c.getColumnIndex(VocardsDataSource.WORD_COLUMN_TEXT)));
		while (! c.isLast()) {
			c.moveToNext();
			this.addNative(c.getString(c.getColumnIndex(VocardsDataSource.WORD_COLUMN_TEXT)));
		}
	}
	
	private void loadForeignWords(Cursor c) {
		c.moveToFirst();
		this.foreignEdit.setText(c.getString(c.getColumnIndex(VocardsDataSource.WORD_COLUMN_TEXT)));
		while (! c.isLast()) {
			c.moveToNext();
			this.addForeign(c.getString(c.getColumnIndex(VocardsDataSource.WORD_COLUMN_TEXT)));
		}
	}
	
	private void addNative(String text) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View cont = inflater.inflate(R.layout.inf_word_add_edit, null, false);
		EditText edit = (EditText) cont.findViewWithTag("edit");
		ImageView minus = (ImageView) cont.findViewWithTag("image");
		
		if (text != null) {
			edit.setText(text);
		}
		
		minus.setOnClickListener(new NativeWordRemoveListener(edit, cont));
		nativeEdits.add(edit);
		nativeContainer.addView(cont);
		
		edit.requestFocus();
	}
	
	private void addForeign(String text) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View cont = inflater.inflate(R.layout.inf_word_add_edit, null, false);
		EditText edit = (EditText) cont.findViewWithTag("edit");
		ImageView minus = (ImageView) cont.findViewWithTag("image");
		
		if (text != null) {
			edit.setText(text);
		}
		
		minus.setOnClickListener(new ForeignWordRemoveListener(edit, cont));
		foreignEdits.add(edit);
		foreignContainer.addView(cont);
		
		edit.requestFocus();
	}
	
	private List<String> getNatWords() {
		List<String> res = new ArrayList<String>();
		for (EditText edit : this.nativeEdits) {
			String str = edit.getText().toString();
			if (! StringUtil.isEmpty(str)) {
				res.add(str);
			}
		}
		return res;
	}
	
	private List<String> getForWords() {
		List<String> res = new ArrayList<String>();
		for (EditText edit : this.foreignEdits) {
			String str = edit.getText().toString();
			if (! StringUtil.isEmpty(str)) {
				res.add(str);
			}
		}
		return res;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	private OnClickListener createClickListener = new OnClickListener() {

		public void onClick(View v) {
			List<String> natWords = getNatWords();
			List<String> forWords = getForWords();
//			
//			String natWord = nativeEdit.getText().toString();
//			String forWord = foreignEdit.getText().toString();

//			if (StringUtil.isEmpty(natWord) || StringUtil.isEmpty(forWord)) {
			if (natWords.isEmpty() || forWords.isEmpty()) {
				Toast.makeText(WordAddActivity.this, R.string.add_word_empty_word_toast, Toast.LENGTH_SHORT).show();
				return;
			}

			if (cardId == -1) {
				WordDS.createCard(db, natWords, forWords, dictId);
			} else {
				WordDS.updateCard(db, cardId, natWords, forWords);
			}
			
			DBUtil.dictModif(db, WordAddActivity.this, dictId);

			Intent i = new Intent(WordAddActivity.this, WordListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
	};

	private OnClickListener nativeAddListener = new OnClickListener() {

		public void onClick(View v) {
			addNative(null);
		}
	};
	
	private OnClickListener foreignAddListener = new OnClickListener() {

		public void onClick(View v) {
			addForeign(null);
		}
	};
	
	private class NativeWordRemoveListener implements OnClickListener {

		private EditText edit;
		private View container;
		
		public NativeWordRemoveListener(EditText edit, View container) {
			super();
			this.edit = edit;
			this.container = container;
		}

		public void onClick(View v) {
			nativeEdits.remove(this.edit);
			nativeContainer.removeView(this.container);
		}
		
	}
	
	private class ForeignWordRemoveListener implements OnClickListener {

		private EditText edit;
		private View container;
		
		public ForeignWordRemoveListener(EditText edit, View container) {
			super();
			this.edit = edit;
			this.container = container;
		}

		public void onClick(View v) {
			foreignEdits.remove(this.edit);
			foreignContainer.removeView(this.container);
		}
		
	}

}
