package cz.cvut.fit.vyhliluk.vocards.util.ds;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

public class WordDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String NATIVE_WORD = "native_word";

	public static final String FOREIGN_WORD = "foreign_word";

	private static final String QUERY_WORDS = "SELECT " +
			"c." + VocardsDataSource.CARD_COLUMN_ID + ", " +
			"nat." + VocardsDataSource.WORD_COLUMN_TEXT + " as " + NATIVE_WORD + ", " +
			"for." + VocardsDataSource.WORD_COLUMN_TEXT + " as " + FOREIGN_WORD + ", " +
			"c."+ VocardsDataSource.CARD_COLUMN_FACTOR + " " +
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
	
	public static long createCard(VocardsDataSource db, String natWord, String forWord, long dictId) {
		db.begin();
		
		ContentValues cardValues = new ContentValues();
		cardValues.put(VocardsDataSource.CARD_COLUMN_DICTIONARY, dictId);
		cardValues.put(VocardsDataSource.CARD_COLUMN_FACTOR, VocardsDataSource.CARD_FACTOR_DEFAULT);
		long cardId = db.insert(VocardsDataSource.CARD_TABLE, cardValues);
		
		ContentValues forWordVal = new ContentValues();
		forWordVal.put(VocardsDataSource.WORD_COLUMN_CARD, cardId);
		forWordVal.put(VocardsDataSource.WORD_COLUMN_TYPE, VocardsDataSource.WORD_TYPE_FOREIGN);
		forWordVal.put(VocardsDataSource.WORD_COLUMN_TEXT, forWord);
		long forWordId = db.insert(VocardsDataSource.WORD_TABLE, forWordVal);
		
		ContentValues natWordVal = new ContentValues();
		natWordVal.put(VocardsDataSource.WORD_COLUMN_CARD, cardId);
		natWordVal.put(VocardsDataSource.WORD_COLUMN_TYPE, VocardsDataSource.WORD_TYPE_NATIVE);
		natWordVal.put(VocardsDataSource.WORD_COLUMN_TEXT, natWord);
		long natWordId = db.insert(VocardsDataSource.WORD_TABLE, natWordVal);
		
		if (natWordId != -1 && forWordId != -1 && cardId != -1) {
			db.commit();
			Log.d("OK", "OK");
			return cardId;
		} else {
			db.rollback();
			Log.d("ROLBACK", "ROLBACK");
			return -1;
		}
	}
	
	public static int removeCard(VocardsDataSource db, long cardId) {
		db.begin();
		
		int res = 0;
		res += db.delete(VocardsDataSource.WORD_TABLE, VocardsDataSource.WORD_COLUMN_CARD+"=?", new String[]{cardId+""});
		res += db.delete(VocardsDataSource.CARD_TABLE, cardId);
		
		db.commit();
		
		return res;
	}
	
	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
