package cz.cvut.fit.vyhliluk.vocards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class VocardsActivity extends Activity {

	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private View wordListIcon = null;
	private View dictListIcon = null;
	private View learnIcon = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.init();
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.wordListIcon = findViewById(R.id.wordList);
		this.dictListIcon = findViewById(R.id.dictList);
		this.learnIcon = findViewById(R.id.learn);

		this.wordListIcon.setOnClickListener(this.wordListClickListener);
		this.dictListIcon.setOnClickListener(this.dictListClickListener);
		this.learnIcon.setOnClickListener(this.learnClickListener);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= VIEW HANDLERS ==========================

	OnClickListener learnClickListener = new OnClickListener() {
		public void onClick(View v) {
			v.setPressed(true);
		}
	};

	OnClickListener wordListClickListener = new OnClickListener() {

		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, WordListActivity.class);
			startActivity(i);
		}
	};

	OnClickListener dictListClickListener = new OnClickListener() {

		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, DictListActivity.class);
			startActivity(i);
		}
	};
}