package cz.cvut.fit.vyhliluk.vocards.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.WordDS;

public class LearnActivity extends AbstractActivity {
	// ================= STATIC ATTRIBUTES ======================

	private static final String KEY_POSITION = "pos";

	// ================= INSTANCE ATTRIBUTES ====================

	private TextView wordCardNat = null;
	private TextView wordCardFor = null;
	private TextView cardFactor = null;
	private TextView cardPos = null;
	private TextView posClickLabel = null;
	private Button nextBtn = null;
	private Button prevBtn = null;

	private Cursor wordCursor = null;
	private long dictId = 0;
	private int position = 0;

	private String orderBy = Settings.getWordOrdering();

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn);

		this.init();

		if (savedInstanceState != null) {
			this.position = savedInstanceState.getInt(KEY_POSITION);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (this.orderBy != null) {
			this.wordCursor = WordDS.getOrdWordsByDictId(this.db, this.dictId, this.orderBy);
		} else {
			this.wordCursor = WordDS.getWordsByDictId(this.db, this.dictId);
		}

		int cnt = this.wordCursor.getCount();
		if (this.position >= cnt) { // User could delete some card
			this.position = cnt - 1;
		}

		this.wordCursor.moveToPosition(this.position);
		this.showCurrentWord();
	}

	@Override
	protected void onPause() {
		this.wordCursor.close();

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_POSITION, this.position);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	/**
	 * Activity initialization
	 */
	private void init() {
		this.wordCardNat = (TextView) findViewById(R.id.wordCardNative);
		this.wordCardFor = (TextView) findViewById(R.id.wordCardForeign);
		this.cardFactor = (TextView) findViewById(R.id.factor);
		this.cardPos = (TextView) findViewById(R.id.position);
		this.nextBtn = (Button) findViewById(R.id.buttonNext);
		this.prevBtn = (Button) findViewById(R.id.buttonPrev);
		this.posClickLabel = (TextView) findViewById(R.id.positionClickLabel);

		this.dictId = Settings.getActiveDictionaryId();
		this.position = Settings.getLearnPosition();

		this.nextBtn.setOnClickListener(this.nextBtnListener);
		this.prevBtn.setOnClickListener(this.prevBtnListener);
		this.posClickLabel.setOnClickListener(this.posClickListener);

		int fontSize = Settings.getCardFontSize();
		this.wordCardNat.setTextSize(fontSize);
		this.wordCardFor.setTextSize(fontSize);
	}

	private void showCurrentWord() {
		int cardCount = this.wordCursor.getCount();
		this.position = this.wordCursor.getPosition();

		Settings.setLearnPosition(this.position);

		String natWord = this.wordCursor.getString(this.wordCursor.getColumnIndex(WordDS.NATIVE_WORD)) + "\u00A0";
		String forWord = this.wordCursor.getString(this.wordCursor.getColumnIndex(WordDS.FOREIGN_WORD)) + "\u00A0";
		int factor = this.wordCursor.getInt(this.wordCursor.getColumnIndex(VocardsDS.CARD_COL_FACTOR));

		this.wordCardNat.setText(natWord);
		this.wordCardFor.setText(forWord);
		this.cardFactor.setText(CardUtil.cardFactorPercent(factor));
		this.cardPos.setText((this.position + 1) + " / " + cardCount);

		this.amendBtnStates();
	}

	private void nextWord() {
		this.wordCursor.moveToNext();
		this.showCurrentWord();
	}

	private void prevWord() {
		this.wordCursor.moveToPrevious();
		this.showCurrentWord();
	}

	private void setPosition(int pos) {
		int cnt = this.wordCursor.getCount();
		if (pos >= cnt) {
			Toast.makeText(this, R.string.learn_chpos_toomuch, Toast.LENGTH_LONG).show();
		} else if (pos < 0) {
			Toast.makeText(this, R.string.learn_chpos_underOne, Toast.LENGTH_LONG).show();
		} else {
			this.wordCursor.moveToPosition(pos);
			this.showCurrentWord();
		}
	}

	private void amendBtnStates() {
		this.nextBtn.setEnabled(!this.wordCursor.isLast());
		this.prevBtn.setEnabled(!this.wordCursor.isFirst());
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	OnClickListener nextBtnListener = new OnClickListener() {

		public void onClick(View v) {
			nextWord();
		}
	};

	OnClickListener prevBtnListener = new OnClickListener() {

		public void onClick(View v) {
			prevWord();
		}
	};

	OnClickListener posClickListener = new OnClickListener() {

		public void onClick(View v) {
			AlertDialog.Builder alert = new AlertDialog.Builder(LearnActivity.this);

			alert.setTitle(R.string.learn_chpos_title);

			// Set an EditText view to get user input
			final EditText input = new EditText(LearnActivity.this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setText("1");
			alert.setView(input);

			alert.setPositiveButton(R.string.learn_chpos_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString();
					try {
						int pos = Integer.parseInt(value);
						setPosition(pos - 1);
					} catch (NumberFormatException ex) {
						Toast.makeText(LearnActivity.this, R.string.learn_chpos_notint, Toast.LENGTH_LONG).show();
					}
				}
			});

			alert.setNegativeButton(R.string.learn_chpos_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			});

			alert.show();
		}
	};

}
