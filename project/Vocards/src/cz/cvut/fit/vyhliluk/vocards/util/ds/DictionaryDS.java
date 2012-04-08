package cz.cvut.fit.vyhliluk.vocards.util.ds;

import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_ID;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NAME;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG;
import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.*;

public class DictionaryDS {
	// ================= STATIC ATTRIBUTES ======================
	
	public static final String WORD_COUNT = "word_count";
	public static final String LEARN_FACTOR = "learn_factor";
	
	private static final String QUERY_STATS = "SELECT " +
			"COUNT("+ VocardsDataSource.CARD_COLUMN_ID +") as "+ WORD_COUNT +"," +
			"AVG("+ VocardsDataSource.CARD_COLUMN_FACTOR +") as "+ LEARN_FACTOR +
			" FROM "+ CARD_TABLE +" WHERE "+ CARD_COLUMN_DICTIONARY +"=?";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getById(VocardsDataSource db, long id) {
		return db.query(
				VocardsDataSource.DICTIONARY_TABLE,
				new String[] { DICTIONARY_COLUMN_ID, DICTIONARY_COLUMN_NAME, DICTIONARY_COLUMN_NATIVE_LANG, DICTIONARY_COLUMN_FOREIGN_LANG },
				DICTIONARY_COLUMN_ID +"=?",
				new String[]{id+""},
				null,
				"1");
	}
	
	public static int getWordCount(VocardsDataSource db, long dictId) {
		Cursor c = db.query(CARD_TABLE, new String[]{CARD_COLUMN_ID}, CARD_COLUMN_DICTIONARY +"=?", new String[]{dictId+""});
		int count = c.getCount();
		c.close();
		return count;
	}
	
	public static Cursor getDictionaryStats(VocardsDataSource db, long dictId) {
		return db.rawQuery(QUERY_STATS, new String[]{dictId+""});
	}
	
	public static double getDictFactor(VocardsDataSource db, long dictId) {
		Cursor c = getDictionaryStats(db, dictId);
		c.moveToFirst();
		double res = c.getDouble(c.getColumnIndex(LEARN_FACTOR));
		c.close();
		return res;
	}

	// ================= CONSTRUCTORS ===========================

	private DictionaryDS() {

	}

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
