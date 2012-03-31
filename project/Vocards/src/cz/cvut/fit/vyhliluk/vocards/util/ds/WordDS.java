package cz.cvut.fit.vyhliluk.vocards.util.ds;

import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

public class WordDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String NATIVE_WORD = "native_word";

	public static final String FOREIGN_WORD = "foreign_word";

	private static final String QUERY_WORDS = "SELECT " +
			"c." + VocardsDataSource.CARD_COLUMN_ID + ", " +
			"nat." + VocardsDataSource.WORD_COLUMN_TEXT + " as " + NATIVE_WORD + ", " +
			"for." + VocardsDataSource.WORD_COLUMN_TEXT + " as " + FOREIGN_WORD + " " +
			"FROM " +
			VocardsDataSource.CARD_TABLE + " c, " +
			VocardsDataSource.WORD_TABLE + " nat, " +
			VocardsDataSource.WORD_TABLE + " for " +
			"WHERE " +
			VocardsDataSource.CARD_COLUMN_DICTIONARY + "=? AND " +
			"nat." + VocardsDataSource.WORD_COLUMN_CARD + "=" + "c." + VocardsDataSource.CARD_COLUMN_ID + " AND " +
			"for." + VocardsDataSource.WORD_COLUMN_CARD + "=" + "c." + VocardsDataSource.CARD_COLUMN_ID + " AND " +
			"nat."+ VocardsDataSource.WORD_COLUMN_TYPE +"="+ VocardsDataSource.WORD_TYPE_NATIVE + " AND "+ 
			"for."+ VocardsDataSource.WORD_COLUMN_TYPE +"="+ VocardsDataSource.WORD_TYPE_FOREIGN;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getWordsByDictId(VocardsDataSource db, long id) {
		return db.rawQuery(QUERY_WORDS, new String[] { id + "" });
	}
	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
