package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

public class VocardsActivity extends AbstractActivity {

	// ================= STATIC ATTRIBUTES ======================
	
	private static final int OPTIONS_SETTINGS = 0;
	
	private static final int REQUEST_DICT_ID = 1;

	// ================= INSTANCE ATTRIBUTES ====================

	private View wordListIcon = null;
	private View dictListIcon = null;
	private View learnIcon = null;
	private View practiseIcon = null;

	private TextView dictNameText = null;
	private TextView wordCountText = null;
	private TextView learnFactorText = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.init();
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.refreshState();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		menu.add(none, OPTIONS_SETTINGS, none, R.string.main_options_settings).setIcon(R.drawable.icon_preferences);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case OPTIONS_SETTINGS:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_DICT_ID) {
			if (resultCode == RESULT_OK) {
				long id = data.getExtras().getLong(DictListActivity.KEY_RESULT_DICT_ID);
				long oldId = Settings.getActiveDictionaryId();
				if (id != oldId) {
					Settings.setActiveDictionaryId(id);
					Settings.removeLearnPosition();
				}
			}
		}
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.wordListIcon = findViewById(R.id.wordList);
		this.dictListIcon = findViewById(R.id.dictList);
		this.learnIcon = findViewById(R.id.learn);
		this.practiseIcon = findViewById(R.id.practise);

		this.dictNameText = (TextView) findViewById(R.id.active_dict_name);
		this.wordCountText = (TextView) findViewById(R.id.word_count);
		this.learnFactorText = (TextView) findViewById(R.id.learn_factor);

		this.wordListIcon.setOnClickListener(this.wordListClickListener);
		this.dictListIcon.setOnClickListener(this.dictListClickListener);
		this.learnIcon.setOnClickListener(this.learnClickListener);
		this.practiseIcon.setOnClickListener(this.practiseClickListener);
	}

	private void refreshState() {
		long activeDictId = Settings.getActiveDictionaryId();

		if (activeDictId == Settings.UNDEFINED_ACTIVE_DICT_ID) {
			this.dictionaryUnselected();
		} else {
			this.dictionarySelected(activeDictId);
		}
	}

	private void dictionarySelected(long id) {
		Cursor c = DictionaryDS.getById(this.db, id);
		if (c.getCount() != 1) { // selected dictionary is not in db
			Settings.removeActiveDictionary();
			this.dictionaryUnselected();
			c.close();
			return;
		}
		c.moveToFirst();
		String name = c.getString(c.getColumnIndex(VocardsDS.DICT_COL_NAME));
		c.close();

		c = DictionaryDS.getDictionaryStats(this.db, id);
		c.moveToFirst();
		int wc = c.getInt(c.getColumnIndex(DictionaryDS.WORD_COUNT));
		double factor = c.getDouble(c.getColumnIndex(DictionaryDS.LEARN_FACTOR));
		c.close();

		this.dictNameText.setText(name);
		this.wordCountText.setText(wc + "");
		this.learnFactorText.setText(CardUtil.dictFactorPercent(factor));
		
		this.wordListIcon.setEnabled(true);
		this.practiseIcon.setEnabled(wc > 0);
		this.learnIcon.setEnabled(wc > 0);
	}

	private void dictionaryUnselected() {
		this.wordListIcon.setEnabled(false);
		this.practiseIcon.setEnabled(false);
		this.learnIcon.setEnabled(false);

		this.dictNameText.setText(R.string.main_no_active_dict);
		this.wordCountText.setText(R.string.main_unknown_value);
		this.learnFactorText.setText(R.string.main_unknown_value);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= VIEW HANDLERS ==========================

	OnClickListener practiseClickListener = new OnClickListener() {
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, PractiseActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
		}
	};

	OnClickListener learnClickListener = new OnClickListener() {
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, LearnActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
		}
	};

	OnClickListener wordListClickListener = new OnClickListener() {
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, WordListActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
		}
	};

	OnClickListener dictListClickListener = new OnClickListener() {
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, DictListActivity.class);
			startActivityForResult(i, REQUEST_DICT_ID);
//			startActivity(i);
			overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
		}
	};
}