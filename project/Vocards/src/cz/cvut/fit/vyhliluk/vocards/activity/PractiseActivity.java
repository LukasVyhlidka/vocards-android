package cz.cvut.fit.vyhliluk.vocards.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.WordDS;

public class PractiseActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private TextView wordCardNat = null;
	private TextView wordCardFor = null;
	private TextView factorText = null;
	private Button otherSideBtn = null;
	private Button knowBtn = null;
	private Button dontKnowBtn = null;

	private String nativeWord = null;
	private String foreignWord = null;
	private long cardId = -1;
	private int factor = -1;

	private long dictId = -1;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.practise);

		this.init();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (this.cardId == -1) {
			this.loadNextWord();
		} else {
			this.loadWord(this.cardId);
		}
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	/**
	 * Activity initialization
	 */
	private void init() {
		this.wordCardNat = (TextView) findViewById(R.id.wordCardNative);
		this.wordCardFor = (TextView) findViewById(R.id.wordCardForeign);
		this.factorText = (TextView) findViewById(R.id.factor);
		this.otherSideBtn = (Button) findViewById(R.id.showOtherSide);
		this.knowBtn = (Button) findViewById(R.id.knowButton);
		this.dontKnowBtn = (Button) findViewById(R.id.dontknowButton);

		this.dictId = Settings.getActiveDictionaryId();

		this.otherSideBtn.setOnClickListener(this.otherSideClickListener);
		this.knowBtn.setOnClickListener(this.knowClickListener);
		this.dontKnowBtn.setOnClickListener(this.dontKnowClickListener);
	}

	private void loadWord(long id) {
		Cursor c = WordDS.getWordById(db, this.dictId, id);
		this.fetchCursor(c);

		this.loadOneSide();
	}

	private void loadNextWord() {
		Cursor c = null;
		int loop = 0;
		do {
			DBUtil.closeExistingCursor(c);
			c = WordDS.getRandomWord(this.db, this.dictId);
			c.moveToFirst();
			loop++;
		} while (c.getLong(c.getColumnIndex(VocardsDataSource.CARD_COLUMN_ID)) == this.cardId && loop < 3);
		this.fetchCursor(c);

		this.loadOneSide();
	}

	private void fetchCursor(Cursor c) {
		c.moveToFirst();
		this.cardId = c.getLong(c.getColumnIndex(VocardsDataSource.CARD_COLUMN_ID));
		this.nativeWord = c.getString(c.getColumnIndex(WordDS.NATIVE_WORD)) + "\u00A0";
		this.foreignWord = c.getString(c.getColumnIndex(WordDS.FOREIGN_WORD))  + "\u00A0";
		this.factor = c.getInt(c.getColumnIndex(VocardsDataSource.CARD_COLUMN_FACTOR));
		c.close();

		this.loadDictFactor();
	}

	private void loadDictFactor() {
		double factor = DictionaryDS.getDictFactor(db, dictId);
		this.factorText.setText(CardUtil.dictFactorPercent(factor));
	}

	private void loadOneSide() {
		this.wordCardNat.setText(this.nativeWord);
		this.wordCardFor.setText("");

		this.visibleOtherSideBtn();
	}

	private void loadOtherSide() {
		this.wordCardFor.setText(this.foreignWord);

		this.visibleKnowledgeBtn();
	}

	private void visibleKnowledgeBtn() {
		this.otherSideBtn.setVisibility(View.GONE);
		this.knowBtn.setVisibility(View.VISIBLE);
		this.dontKnowBtn.setVisibility(View.VISIBLE);
	}

	private void visibleOtherSideBtn() {
		this.otherSideBtn.setVisibility(View.VISIBLE);
		this.knowBtn.setVisibility(View.GONE);
		this.dontKnowBtn.setVisibility(View.GONE);
	}

	private void know() {
		int newFactor = CardUtil.getNewFactor(true, this.factor);
		WordDS.updateFactor(this.db, this.cardId, newFactor);

		this.loadNextWord();
	}

	private void dontKnow() {
		int newFactor = CardUtil.getNewFactor(false, this.factor);
		WordDS.updateFactor(this.db, this.cardId, newFactor);

		this.loadNextWord();
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	OnClickListener otherSideClickListener = new OnClickListener() {

		public void onClick(View v) {
			loadOtherSide();
		}
	};

	OnClickListener knowClickListener = new OnClickListener() {

		public void onClick(View v) {
			know();
		}
	};

	OnClickListener dontKnowClickListener = new OnClickListener() {

		public void onClick(View v) {
			dontKnow();
		}
	};

}