package cz.cvut.fit.vyhliluk.vocards.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.activity.task.TranslateTask;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.AppStatus;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.StringUtil;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;
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

	private ProgressBar progressBar = null;

	private List<EditText> nativeEdits = new ArrayList<EditText>();
	private List<EditText> foreignEdits = new ArrayList<EditText>();

	private long cardId = -1;
	private long dictId = -1;

	private MyTranslateTask translateTask = null;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.word_add);

		this.init();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (this.translateTask != null && !this.translateTask.getStatus().equals(Status.FINISHED)) {
			this.translateTask.detach();
			return this.translateTask;
		} else if (this.translateTask != null) {
			this.translateTask.dismissDialog();
		}
		return super.onRetainNonConfigurationInstance();
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.nativeEdit = (EditText) findViewById(R.id.nativeEdit);
		this.foreignEdit = (EditText) findViewById(R.id.foreignEdit);
		this.nativeContainer = (LinearLayout) findViewById(R.id.nativeContainer);
		this.foreignContainer = (LinearLayout) findViewById(R.id.foreignContainer);
		this.progressBar = (ProgressBar) findViewById(R.id.translateProgress);

		this.nativeEdits.add(this.nativeEdit);
		this.foreignEdits.add(this.foreignEdit);

		Button createBtn = (Button) findViewById(R.id.buttonAdd);
		createBtn.setOnClickListener(this.createClickListener);

		ImageView nativeAdd = (ImageView) findViewById(R.id.nativeAdd);
		nativeAdd.setOnClickListener(this.nativeAddListener);

		ImageView foreignAdd = (ImageView) findViewById(R.id.foreignAdd);
		foreignAdd.setOnClickListener(this.foreignAddListener);

		this.dictId = Settings.getActiveDictionaryId();

		if (getLastNonConfigurationInstance() != null) {
			this.translateTask = (MyTranslateTask) getLastNonConfigurationInstance();
			this.translateTask.attach(this);
		} else {
			Intent i = this.getIntent();
			Bundle b = i.getExtras();
			if (b != null) {
				this.handleBundle(b);
			}
		}
	}

	private void handleBundle(Bundle b) {
		if (b.containsKey(EXTRAS_NATIVE_WORD)) {
			String natWord = b.getString(EXTRAS_NATIVE_WORD);
			this.nativeEdit.setText(natWord);
			this.foreignEdit.requestFocus();
			this.translate(natWord);
		} else if (b.containsKey(EXTRAS_CARD_ID)) {
			this.cardId = b.getLong(EXTRAS_CARD_ID);
			
			Cursor c = WordDS.getCardById(db, this.cardId);
			c.moveToFirst();
			
			List<String> natWords = CardUtil.explodeWords(c.getString(c.getColumnIndex(VocardsDS.CARD_COL_NATIVE)));
			List<String> forWords = CardUtil.explodeWords(c.getString(c.getColumnIndex(VocardsDS.CARD_COL_FOREIGN)));
			
			c.close();

			this.loadNativeWords(natWords);
			this.loadForeignWords(forWords);
		}
	}

	private void translate(String word) {
		if (! Settings.getTranslation() || !AppStatus.isOnline(this)) {
			return;
		}

		Cursor c = DictionaryDS.getById(db, dictId);
		c.moveToFirst();
		Language from = Language.getById(c.getInt(c.getColumnIndex(VocardsDS.DICT_COL_NATIVE_LANG)));
		Language to = Language.getById(c.getInt(c.getColumnIndex(VocardsDS.DICT_COL_FOREIGN_LANG)));
		c.close();
		if (!Language.NONE.equals(from) && !Language.NONE.equals(to)) {
			this.translateTask = new MyTranslateTask(from, to);
			this.translateTask.attach(this);
			this.translateTask.execute(word);
		}
	}

	private void loadNativeWords(List<String> words) {
		String first = words.get(0);
		this.nativeEdit.setText(first);
		words.remove(0);
		for (String word : words) {
			this.addNative(word);
		}
	}

	private void loadForeignWords(List<String> words) {
		String first = words.get(0);
		this.foreignEdit.setText(first);
		words.remove(0);
		for (String word : words) {
			this.addForeign(word);
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
			if (!StringUtil.isEmpty(str)) {
				res.add(str);
			}
		}
		return res;
	}

	private List<String> getForWords() {
		List<String> res = new ArrayList<String>();
		for (EditText edit : this.foreignEdits) {
			String str = edit.getText().toString();
			if (!StringUtil.isEmpty(str)) {
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

	private class MyTranslateTask extends TranslateTask {

		private Context ctx;
		private AlertDialog alertDialog = null;
		
		public MyTranslateTask(Language from, Language to) {
			super(from, to);
		}
		
		public void detach() {
			this.ctx = null;
			this.dismissDialog();
		}
		
		public void attach(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			super.onPostExecute(result);

			progressBar.setVisibility(View.INVISIBLE);
			if (!result.isEmpty()) {
				final String[] items = result.toArray(new String[] {});

				AlertDialog.Builder builder = new AlertDialog.Builder(this.ctx);
				builder.setTitle(R.string.add_word_translation_pick_title);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String word = items[item];
						foreignEdit.setText(word);
					}
				});
				alertDialog = builder.create();
				alertDialog.show();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressBar.setVisibility(View.VISIBLE);
		}
		
		public void dismissDialog() {
			if (this.alertDialog != null && this.alertDialog.isShowing()) {
				this.alertDialog.dismiss();
				this.alertDialog = null;
			}
		}

	}

}
