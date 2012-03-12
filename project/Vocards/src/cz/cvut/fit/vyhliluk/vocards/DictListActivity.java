package cz.cvut.fit.vyhliluk.vocards;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Activity class
 * @author Lucky
 *
 */
public class DictListActivity extends Activity {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================
	
	private ListView dictList = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_list);
		
		this.init();
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================
	
	private void init() {
		this.dictList = (ListView)findViewById(R.id.dictList);
		
		String[] values = new String[] { "Dict 1", "Dict 2", "Dict 3",
				"Dict 4", "Dict 5", "Dict 6", "Dict 7", "Dict 8", "Dict 9",
				"Dict 10", "Dict 11", "Dict 12", "Dict 13", "Dict 14",
				"Dict 15", "Dict 16", "Dict 17", "Dict 18", "Dict 19",
				"Dict 20" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		
		this.dictList.setAdapter(adapter);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================
}
