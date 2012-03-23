package cz.cvut.fit.vyhliluk.vocards;

import cz.cvut.fit.vyhliluk.vocards.abstr.AbstractActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class VocardsActivity extends AbstractActivity {

	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private View wordListIcon = null;
	private View dictListIcon = null;
	private View learnIcon = null;
	private View practiseIcon = null;

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
		this.practiseIcon = findViewById(R.id.practise);

		this.wordListIcon.setOnClickListener(this.wordListClickListener);
		this.dictListIcon.setOnClickListener(this.dictListClickListener);
		this.learnIcon.setOnClickListener(this.learnClickListener);
		this.practiseIcon.setOnClickListener(this.practiseClickListener);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= VIEW HANDLERS ==========================

	OnClickListener practiseClickListener = new OnClickListener() {
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, PractiseActivity.class);
			startActivity(i);
		}
	};
	
	OnClickListener learnClickListener = new OnClickListener() {
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, LearnActivity.class);
			startActivity(i);
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