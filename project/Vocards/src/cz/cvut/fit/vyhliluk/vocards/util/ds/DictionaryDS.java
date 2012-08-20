package cz.cvut.fit.vyhliluk.vocards.util.ds;

import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_COLUMN_DICTIONARY;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.CARD_TABLE;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_ID;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NAME;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.core.ParentIsDescendantException;
import cz.cvut.fit.vyhliluk.vocards.core.ParentIsTheSameException;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
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

	public static final String QUERY_DICT_DESCENDANT_IDS = QUERY_DICT_DESCENDANT_OR_SELF_IDS +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + "!=0";

	public static final String QUERY_DICT_ROOT = "SELECT " +
			"c.* FROM " + VocardsDataSource.DICTIONARY_TABLE + " c " +
			"WHERE NOT EXISTS( SELECT " +
			VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " " +
			"FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "= c." + VocardsDataSource.DICTIONARY_COLUMN_ID +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + " != " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " " +
			")";

	public static final String QUERY_ANCESTOR_OR_SELF_DICT_ID = "SELECT " +
			VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " " +
			"FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "=?";

	public static final String QUERY_ANCESTOR_DICT_ID = QUERY_ANCESTOR_OR_SELF_DICT_ID +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + ">0";

	public static final String QUERY_PARENT_DICT_ID = QUERY_ANCESTOR_OR_SELF_DICT_ID +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + "=1";

	private static final String QUERY_STATS = "SELECT " +
			"COUNT(" + VocardsDataSource.CARD_COLUMN_ID + ") as " + WORD_COUNT + "," +
			"AVG(" + VocardsDataSource.CARD_COLUMN_FACTOR + ") as " + LEARN_FACTOR +
			" FROM " + CARD_TABLE + " WHERE " + CARD_COLUMN_DICTIONARY + " IN (" + QUERY_DICT_DESCENDANT_OR_SELF_IDS + ")";

//	private static final String INSERT_HIERARCHY_FROM_PARENT = "INSERT INTO " + VocardsDataSource.HIERARCHY_TABLE + " " +
//			"(" +
//			VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + ", " +
//			VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + ", " +
//			VocardsDataSource.HIERARCHY_COLUMN_LENGTH +
//			") SELECT " +
//			VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + ", " +
//			"?, " +
//			"(" + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + " + 1) " +
//			"FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
//			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "=?";
	
	private static final String INSERT_HIERARCHY_TO_BE_CHILD_OF = "INSERT INTO " + VocardsDataSource.HIERARCHY_TABLE + " (" +
			VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "," +
			VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "," +
			VocardsDataSource.HIERARCHY_COLUMN_LENGTH +
			") " +
			"SELECT " +
			"a." + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "," +
			"d." + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "," +
			"(ld." + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + " + la." + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + " + 1) " +
			"FROM " +
			VocardsDataSource.HIERARCHY_TABLE + " d," +
			VocardsDataSource.HIERARCHY_TABLE + " a," +
			VocardsDataSource.HIERARCHY_TABLE + " ld," +
			VocardsDataSource.HIERARCHY_TABLE + " la " +
			"WHERE" +
			" d." + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "=?" +
			" AND a." + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "=?" +
			" AND ld." + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "=d." + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR +
			" AND ld." + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "=d." + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT +
			" AND la." + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "=a." + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT +
			" AND la." + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "=a." + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR;

	private static final String REMOVE_DICT_HIERARCHY = "DELETE FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + "=? " +
			"OR " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + "=?";

	private static final String UPDATE_DICT_DESCENDANTS_HIERARCHY = "UPDATE " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"SET " + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + "=" + VocardsDataSource.HIERARCHY_COLUMN_LENGTH + "-1 " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + " IN (" + QUERY_DICT_CHILD_IDS + ")" +
			" AND " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " != " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT;

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
			return db.rawQuery(QUERY_DICT_ROOT, new String[] {});
		} else {
			return db.query(
					VocardsDataSource.DICTIONARY_TABLE,
					null,
					VocardsDataSource.DICTIONARY_COLUMN_ID + " IN (" + QUERY_DICT_CHILD_IDS + ")",
					new String[] { rootDictId + "" });
		}
	}

	public static Cursor getDescendantDictionaries(VocardsDataSource db, Long dictId) {
		return db.query(
				VocardsDataSource.DICTIONARY_TABLE,
				null,
				VocardsDataSource.DICTIONARY_COLUMN_ID + " IN (" + QUERY_DICT_DESCENDANT_IDS + ")",
				new String[] { dictId + "" });
	}

	public static Cursor getParentDictionary(VocardsDataSource db, Long dictId) {
		return db.query(
				VocardsDataSource.DICTIONARY_TABLE,
				null,
				VocardsDataSource.DICTIONARY_COLUMN_ID + "= (" + QUERY_PARENT_DICT_ID + ")",
				new String[] { dictId + "" });
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

	public static long createDictionary(VocardsDataSource db, String name, Language nativeLang, Language foreignLang,
			Long parentDictId) {
		db.begin();

		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NAME, name);
		val.put(VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG, foreignLang.getId());
		long id = db.insert(VocardsDataSource.DICTIONARY_TABLE, val);

		val = new ContentValues();
		val.put(VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR, id);
		val.put(VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT, id);
		val.put(VocardsDataSource.HIERARCHY_COLUMN_LENGTH, 0);
		db.insert(VocardsDataSource.HIERARCHY_TABLE, val);

		if (parentDictId != null) {
//			db.execSql(INSERT_HIERARCHY_FROM_PARENT, new String[] { id + "", parentDictId + "" });
			try {
				setAsChildOf(db, id, parentDictId);
			} catch (VocardsException ex) {
				Log.e("dict creation", "setAsChildOf method error.", ex);
			}
		}

		db.commit();
		return id;
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

	/**
	 * 
	 * @param db
	 * @param dictId
	 * @return
	 */
	public static int deleteDict(VocardsDataSource db, long dictId, boolean descendants) {
		int res = 0;

		db.begin();
		if (descendants) {
			Cursor descs = getDescendantDictionaries(db, dictId);
			descs.moveToFirst();
			while (!descs.isAfterLast()) {
				long descId = descs.getLong(descs.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_ID));
				deleteDict(db, descId, false);
				descs.moveToNext();
			}
			descs.close();
		}
		BackupDS.setDeleted(db, dictId);

		db.execSql(UPDATE_DICT_DESCENDANTS_HIERARCHY, new String[] { dictId + "" });
		db.execSql(REMOVE_DICT_HIERARCHY, new String[] { dictId + "", dictId + "" });

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

	public static int setAsRoot(VocardsDataSource db, long id) {
		String where = VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + " IN (" +
				QUERY_DICT_DESCENDANT_OR_SELF_IDS +
				") " +
				"AND " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR + " IN (" +
				QUERY_ANCESTOR_DICT_ID +
				")";

		db.begin();
		int deleted = db.delete(
				VocardsDataSource.HIERARCHY_TABLE,
				where,
				new String[] { id + "", id + "" }
				);
		db.commit();
		return deleted;
	}

	public static void setAsChildOf(VocardsDataSource db, long movedDictId, long parentDictId) 
			throws ParentIsTheSameException, ParentIsDescendantException {
		db.begin();

		//Verify that moved and parent dictionaries are not the same!
		if (movedDictId == parentDictId) {
			throw new ParentIsTheSameException("Parent and moved dictionaries are the same!");
		}
		
		//Verify that parent dictionary is not a descendant of moved dictionary!
		Cursor descs = getDescendantDictionaries(db, movedDictId);
		descs.moveToFirst();
		while (!descs.isAfterLast()) {
			long descId = descs.getLong(descs.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_ID));
			if (descId == parentDictId) {
				descs.close();
				throw new ParentIsDescendantException("Parent is a descendant of moved dictionary!");
			}
			descs.moveToNext();
		}
		descs.close();

		setAsRoot(db, movedDictId);
		db.execSql(INSERT_HIERARCHY_TO_BE_CHILD_OF, new String[] { movedDictId + "", parentDictId + "" });
		
		db.commit();
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
