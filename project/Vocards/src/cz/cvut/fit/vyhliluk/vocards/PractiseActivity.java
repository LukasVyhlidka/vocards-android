package cz.cvut.fit.vyhliluk.vocards;

import android.os.Bundle;
import android.widget.TextView;

public class PractiseActivity extends AbstractActivity {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================
	
	private TextView wordCardText = null;

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.practise);

		this.init();
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
