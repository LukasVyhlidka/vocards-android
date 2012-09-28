package cz.cvut.fit.vyhliluk.vocards.persistence;

import java.util.List;

import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VocardsDSMigrator {

	// ==================== STATIC ATTRIBUTES ==================

	// ==================== INSTANCE ATTRIBUTES ================

	// ==================== STATIC METHODS =====================

	public static void version4(SQLiteDatabase db) {

		db.beginTransaction();

		db.execSQL(VocardsDS.CREATE_WORD);
		db.execSQL(VocardsDS.CREATE_WORD_CARD_ID_INDEX);

		Cursor c = db.query(VocardsDS.CARD_TABLE, null, null, null, null, null, null);
		while (c.moveToNext()) {
			long cardId = c.getLong(c.getColumnIndex(VocardsDS.CARD_COL_ID));

			String nat = c.getString(c.getColumnIndex(VocardsDS.CARD_COL_NATIVE));
			List<String> natWords = CardUtil.explodeWords(nat);
			for (String word : natWords) {
				version4WordCreate(db, cardId, VocardsDS.WORD_TYPE_NAT, word);
			}

			String forW = c.getString(c.getColumnIndex(VocardsDS.CARD_COL_FOREIGN));
			List<String> forWords = CardUtil.explodeWords(forW);
			for (String word : forWords) {
				version4WordCreate(db, cardId, VocardsDS.WORD_TYPE_FOR, word);
			}
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	// ==================== CONSTRUCTORS =======================

	// ==================== OVERRIDEN METHODS ==================

	// ==================== INSTANCE METHODS ===================

	// ==================== PRIVATE METHODS ====================

	private static void version4WordCreate(SQLiteDatabase db, long cardId, int type, String word) {
		ContentValues val = new ContentValues();
		val.put(VocardsDS.WORD_COL_CARD_ID, cardId);
		val.put(VocardsDS.WORD_COL_TYPE, type);
		val.put(VocardsDS.WORD_COL_WORD, word);
		
		db.insert(VocardsDS.WORD_TABLE, null, val);
	}

	// ==================== GETTERS/SETTERS ====================
}
