package cz.cvut.fit.vyhliluk.vocards;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WordListActivity extends Activity {

	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================
	
	private ListView wordList = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.word_list);
		
		this.init();
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================
	
	private void init() {
		this.wordList = (ListView)findViewById(R.id.wordList);
		
		String[] values = new String[] { "Item 1", "Item 2", "Item 3",
				"Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9",
				"Item 10", "Item 11", "Item 12", "Item 13", "Item 14",
				"Item 15", "Item 16", "Item 17", "Item 18", "Item 19",
				"Item 20" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		
		this.wordList.setAdapter(adapter);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
