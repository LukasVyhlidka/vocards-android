package cz.cvut.fit.vyhliluk.vocards;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

public abstract class AbstractActivity extends Activity {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================
	
	protected Resources res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.res = this.getResources();
	}

	//================= CONSTRUCTORS ===========================
	

	//================= OVERRIDEN METHODS ======================
	
	

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
