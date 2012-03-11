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
		this.wordListIcon.setClickable(true);
		
		this.wordListIcon.setOnClickListener(this.wordListClickListener);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= VIEW HANDLERS ==========================
	
	OnClickListener wordListClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			Context ctx = VocardsActivity.this;
			Intent i = new Intent(ctx, WordListActivity.class);
			startActivity(i);
		}
	};
}