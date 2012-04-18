package cz.cvut.fit.vyhliluk.vocards.util.ds;

import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_COLUMN_DICTIONARY;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_COLUMN_ID;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_TABLE;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_ID;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NAME;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

public class DictionaryDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String WORD_COUNT = "word_count";
	public static final String LEARN_FACTOR = "learn_factor";

	private static final String QUERY_STATS = "SELECT " +
			"COUNT(" + VocardsDataSource.CARD_COLUMN_ID + ") as " + WORD_COUNT + "," +
			"AVG(" + VocardsDataSource.CARD_COLUMN_FACTOR + ") as " + LEARN_FACTOR +
			" FROM " + CARD_TABLE + " WHERE " + CARD_COLUMN_DICTIONARY + "=?";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getByName(VocardsDataSource db, String name) {
		return db.query(
				VocardsDataSource.DICTIONARY_TABLE,
				null,
				DICTIONARY_COLUMN_NAME + "=?",
				new String[] { name });
	}

	public static Cursor getById(VocardsDataSource db, long id) {
		return db.query(
				VocardsDataSource.DICTIONARY_TABLE,
				new String[] { DICTIONARY_COLUMN_ID, DICTIONARY_COLUMN_NAME, DICTIONARY_COLUMN_NATIVE_LANG, DICTIONARY_COLUMN_FOREIGN_LANG },
				DICTIONARY_COLUMN_ID + "=?",
				new String[] { id + "" },
				null,
				"1");
	}

	public static int getWordCount(VocardsDataSource db, long dictId) {
		Cursor c = db.query(CARD_TABLE, new String[] { CARD_COLUMN_ID }, CARD_COLUMN_DICTIONARY + "=?", new String[] { dictId + "" });
		int count = c.getCount();
		c.close();
		return count;
	}

	public static Cursor getDictionaryStats(VocardsDataSource db, long dictId) {
		return db.rawQuery(QUERY_STATS, new String[] { dictId + "" });
	}

	public static double getDictFactor(VocardsDataSource db, long dictId) {
		Cursor c = getDictionaryStats(db, dictId);
		c.moveToFirst();
		double res = c.getDouble(c.getColumnIndex(LEARN_FACTOR));
		c.close();
		return res;
	}

	public static Cursor getDictionaries(VocardsDataSource db) {
		Cursor c = db.query(VocardsDataSource.DICTIONARY_TABLE,
				new String[] {
						VocardsDataSource.DICTIONARY_COLUMN_ID,
						VocardsDataSource.DICTIONARY_COLUMN_NAME,
						VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG },
				null,
				null,
				VocardsDataSource.DICTIONARY_COLUMN_NAME);
		return c;
	}

	public static List<Long> getDictIds(VocardsDataSource db) {
		Cursor c = getDictionaries(db);
		List<Long> res = new ArrayList<Long>();
		c.moveToNext();
		while (! c.isAfterLast()) {
			res.add(c.getLong(c.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_ID)));
		}
		c.close();
		return res;
	}

	public static long createDictionary(VocardsDataSource db, String name, Language nativeLang, Language foreignLang) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NAME, name);
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG, foreignLang.getId());
		return db.insert(VocardsDataSource.DICTIONARY_TABLE, val);
	}

	public static int deleteDict(VocardsDataSource db, long dictId) {
		int res = 0;

		db.begin();
		res += WordDS.removeCardsByDict(db, dictId);
		res += db.delete(VocardsDataSource.DICTIONARY_TABLE, dictId);
		db.commit();

		return res;
	}

	public static int updateDictionary(VocardsDataSource db, long id, String name, Language nativeLang, Language foreignLang) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NAME, name);
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG, foreignLang.getId());
		return db.update(VocardsDataSource.DICTIONARY_TABLE, val, VocardsDataSource.DICTIONARY_COLUMN_ID + "=?", new String[] { id + "" });
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
