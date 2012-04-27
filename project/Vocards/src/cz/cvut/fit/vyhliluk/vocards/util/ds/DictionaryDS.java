package cz.cvut.fit.vyhliluk.vocards.util.ds;

import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_COLUMN_DICTIONARY;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_TABLE;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_ID;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NAME;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG;
import android.content.ContentValues;
import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

public class DictionaryDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String WORD_COUNT = "word_count";
	public static final String LEARN_FACTOR = "learn_factor";

	public static final String QUERY_DICT_DESCENDANT_OR_SELF_IDS = "SELECT " +
			VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + " " +
			"FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "=?";

	public static final String QUERY_DICT_CHILD_IDS = QUERY_DICT_DESCENDANT_OR_SELF_IDS +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + "=1";

	public static final String QUERY_DICT_ROOT = "SELECT " +
			"c.* FROM " + VocardsDataSource.DICTIONARY_TABLE + " c" +
			"WHERE NOT EXISTS( " +
			VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " " +
			"FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "= c."+ VocardsDataSource.DICTIONARY_COLUMN_ID +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + " != " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " " +
			")";

	private static final String QUERY_STATS = "SELECT " +
			"COUNT(" + VocardsDataSource.CARD_COLUMN_ID + ") as " + WORD_COUNT + "," +
			"AVG(" + VocardsDataSource.CARD_COLUMN_FACTOR + ") as " + LEARN_FACTOR +
			" FROM " + CARD_TABLE + " WHERE " + CARD_COLUMN_DICTIONARY + " IN (" + QUERY_DICT_DESCENDANT_OR_SELF_IDS + ")";

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
		Cursor c = getDictionaryStats(db, dictId);
		c.moveToFirst();
		int count = c.getInt(c.getColumnIndex(WORD_COUNT));
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

	public static Cursor getChildDictionaries(VocardsDataSource db, Long rootDictId) {
		if (rootDictId == null) {
			return db.rawQuery(QUERY_DICT_ROOT, new String[]{});
		} else {
			return db.query(
					VocardsDataSource.DICTIONARY_TABLE,
					null,
					VocardsDataSource.DICTIONARY_COLUMN_ID + " IN (" + QUERY_DICT_CHILD_IDS + ")",
					new String[] { rootDictId + "" });
		}
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

	public static Cursor getModifiedDicts(VocardsDataSource db, long lastBackup) {
		return db.query(
				VocardsDataSource.DICTIONARY_TABLE,
				null,
				VocardsDataSource.DICTIONARY_COLUMN_MODIFIED + ">? OR " + VocardsDataSource.DICTIONARY_COLUMN_MODIFIED + " IS NULL",
				new String[] { lastBackup + "" });
	}

	public static long createDictionary(VocardsDataSource db, String name, Language nativeLang, Language foreignLang) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NAME, name);
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG, foreignLang.getId());
		return db.insert(VocardsDataSource.DICTIONARY_TABLE, val);
	}

	public static void setModified(VocardsDataSource db, long dictId) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.DICTIONARY_COLUMN_MODIFIED, System.currentTimeMillis());
		db.update(
				VocardsDataSource.DICTIONARY_TABLE,
				val,
				VocardsDataSource.DICTIONARY_COLUMN_ID + "=?",
				new String[] { dictId + "" });
	}

	public static int deleteDict(VocardsDataSource db, long dictId) {
		int res = 0;

		db.begin();
		BackupDS.setDeleted(db, dictId);
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
