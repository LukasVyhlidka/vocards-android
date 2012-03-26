package cz.cvut.fit.vyhliluk.vocards.activity;

import android.os.Bundle;
import android.widget.TextView;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.activity.abstr.AbstractActivity;

public class LearnActivity extends AbstractActivity {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================
	
	private TextView wordCardText = null;

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn);

		this.init();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================
	
	/**
	 * Activity initialization
	 */
	private void init() {
		this.wordCardText = (TextView) findViewById(R.id.wordCardText);
	}

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
