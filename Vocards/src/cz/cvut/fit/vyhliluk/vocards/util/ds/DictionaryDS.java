package cz.cvut.fit.vyhliluk.vocards.util.ds;

import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS.CARD_COL_DICTIONARY;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS.CARD_TABLE;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS.DICT_COL_FOREIGN_LANG;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS.DICT_COL_ID;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS.DICT_COL_NAME;
import static cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS.DICT_COL_NATIVE_LANG;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.core.ParentIsDescendantException;
import cz.cvut.fit.vyhliluk.vocards.core.ParentIsTheSameException;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;

public class DictionaryDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String WORD_COUNT = "word_count";
	public static final String LEARN_FACTOR = "learn_factor";

	public static final String QUERY_DICT_DESCENDANT_OR_SELF_IDS = "SELECT " +
			VocardsDS.HIER_COL_DESCENDANT + " " +
			"FROM " + VocardsDS.HIER_TABLE + " " +
			"WHERE " + VocardsDS.HIER_COL_ANCESTOR + "=?";

	public static final String QUERY_DICT_CHILD_IDS = QUERY_DICT_DESCENDANT_OR_SELF_IDS +
			" AND " + VocardsDS.HIER_COL_LENGTH + "=1";

	public static final String QUERY_DICT_DESCENDANT_IDS = QUERY_DICT_DESCENDANT_OR_SELF_IDS +
			" AND " + VocardsDS.HIER_COL_LENGTH + "!=0";

	public static final String QUERY_DICT_ROOT = "SELECT " +
			"c.* FROM " + VocardsDS.DICT_TABLE + " c " +
			"WHERE NOT EXISTS( SELECT " +
			VocardsDS.HIER_COL_ANCESTOR + " " +
			"FROM " + VocardsDS.HIER_TABLE + " " +
			"WHERE " + VocardsDS.HIER_COL_DESCENDANT + "= c." + VocardsDS.DICT_COL_ID +
			" AND " + VocardsDS.HIER_COL_DESCENDANT + " != " + VocardsDS.HIER_COL_ANCESTOR + " " +
			")";

	public static final String QUERY_ANCESTOR_OR_SELF_DICT_ID = "SELECT " +
			VocardsDS.HIER_COL_ANCESTOR + " " +
			"FROM " + VocardsDS.HIER_TABLE + " " +
			"WHERE " + VocardsDS.HIER_COL_DESCENDANT + "=?";

	public static final String QUERY_ANCESTOR_DICT_ID = QUERY_ANCESTOR_OR_SELF_DICT_ID +
			" AND " + VocardsDS.HIER_COL_LENGTH + ">0";

	public static final String QUERY_PARENT_DICT_ID = QUERY_ANCESTOR_OR_SELF_DICT_ID +
			" AND " + VocardsDS.HIER_COL_LENGTH + "=1";

	private static final String QUERY_STATS = "SELECT " +
			"COUNT(" + VocardsDS.CARD_COL_ID + ") as " + WORD_COUNT + "," +
			"AVG(" + VocardsDS.CARD_COL_FACTOR + ") as " + LEARN_FACTOR +
			" FROM " + CARD_TABLE + " WHERE " + CARD_COL_DICTIONARY + " IN (" + QUERY_DICT_DESCENDANT_OR_SELF_IDS + ")";
	
	private static final String INSERT_HIERARCHY_TO_BE_CHILD_OF = "INSERT INTO " + VocardsDS.HIER_TABLE + " (" +
			VocardsDS.HIER_COL_ANCESTOR + "," +
			VocardsDS.HIER_COL_DESCENDANT + "," +
			VocardsDS.HIER_COL_LENGTH +
			") " +
			"SELECT " +
			"a." + VocardsDS.HIER_COL_ANCESTOR + "," +
			"d." + VocardsDS.HIER_COL_DESCENDANT + "," +
			"(ld." + VocardsDS.HIER_COL_LENGTH + " + la." + VocardsDS.HIER_COL_LENGTH + " + 1) " +
			"FROM " +
			VocardsDS.HIER_TABLE + " d," +
			VocardsDS.HIER_TABLE + " a," +
			VocardsDS.HIER_TABLE + " ld," +
			VocardsDS.HIER_TABLE + " la " +
			"WHERE" +
			" d." + VocardsDS.HIER_COL_ANCESTOR + "=?" +
			" AND a." + VocardsDS.HIER_COL_DESCENDANT + "=?" +
			" AND ld." + VocardsDS.HIER_COL_ANCESTOR + "=d." + VocardsDS.HIER_COL_ANCESTOR +
			" AND ld." + VocardsDS.HIER_COL_DESCENDANT + "=d." + VocardsDS.HIER_COL_DESCENDANT +
			" AND la." + VocardsDS.HIER_COL_DESCENDANT + "=a." + VocardsDS.HIER_COL_DESCENDANT +
			" AND la." + VocardsDS.HIER_COL_ANCESTOR + "=a." + VocardsDS.HIER_COL_ANCESTOR;

	private static final String REMOVE_DICT_HIERARCHY = "DELETE FROM " + VocardsDS.HIER_TABLE + " " +
			"WHERE " + VocardsDS.HIER_COL_ANCESTOR + "=? " +
			"OR " + VocardsDS.HIER_COL_DESCENDANT + "=?";

	private static final String UPDATE_DICT_DESCENDANTS_HIERARCHY = "UPDATE " + VocardsDS.HIER_TABLE + " " +
			"SET " + VocardsDS.HIER_COL_LENGTH + "=" + VocardsDS.HIER_COL_LENGTH + "-1 " +
			"WHERE " + VocardsDS.HIER_COL_DESCENDANT + " IN (" + QUERY_DICT_CHILD_IDS + ")" +
			" AND " + VocardsDS.HIER_COL_ANCESTOR + " != " + VocardsDS.HIER_COL_DESCENDANT;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getByName(VocardsDS db, String name) {
		return db.query(
				VocardsDS.DICT_TABLE,
				null,
				DICT_COL_NAME + "=?",
				new String[] { name });
	}

	public static Cursor getById(VocardsDS db, long id) {
		return db.query(
				VocardsDS.DICT_TABLE,
				new String[] { DICT_COL_ID, DICT_COL_NAME, DICT_COL_NATIVE_LANG, DICT_COL_FOREIGN_LANG },
				DICT_COL_ID + "=?",
				new String[] { id + "" },
				null,
				"1");
	}

	public static int getWordCount(VocardsDS db, long dictId) {
		Cursor c = getDictionaryStats(db, dictId);
		c.moveToFirst();
		int count = c.getInt(c.getColumnIndex(WORD_COUNT));
		c.close();
		return count;
	}

	public static Cursor getDictionaryStats(VocardsDS db, long dictId) {
		return db.rawQuery(QUERY_STATS, new String[] { dictId + "" });
	}

	public static double getDictFactor(VocardsDS db, long dictId) {
		Cursor c = getDictionaryStats(db, dictId);
		c.moveToFirst();
		double res = c.getDouble(c.getColumnIndex(LEARN_FACTOR));
		c.close();
		return res;
	}

	public static Cursor getChildDictionaries(VocardsDS db, Long rootDictId) {
		if (rootDictId == null) {
			return db.rawQuery(QUERY_DICT_ROOT, new String[] {});
		} else {
			return db.query(
					VocardsDS.DICT_TABLE,
					null,
					VocardsDS.DICT_COL_ID + " IN (" + QUERY_DICT_CHILD_IDS + ")",
					new String[] { rootDictId + "" });
		}
	}

	public static Cursor getDescendantDictionaries(VocardsDS db, Long dictId) {
		return db.query(
				VocardsDS.DICT_TABLE,
				null,
				VocardsDS.DICT_COL_ID + " IN (" + QUERY_DICT_DESCENDANT_IDS + ")",
				new String[] { dictId + "" });
	}

	public static Cursor getParentDictionary(VocardsDS db, Long dictId) {
		return db.query(
				VocardsDS.DICT_TABLE,
				null,
				VocardsDS.DICT_COL_ID + "= (" + QUERY_PARENT_DICT_ID + ")",
				new String[] { dictId + "" });
	}

	public static Cursor getDictionaries(VocardsDS db) {
		Cursor c = db.query(VocardsDS.DICT_TABLE,
				new String[] {
						VocardsDS.DICT_COL_ID,
						VocardsDS.DICT_COL_NAME,
						VocardsDS.DICT_COL_NATIVE_LANG,
						VocardsDS.DICT_COL_FOREIGN_LANG },
				null,
				null,
				VocardsDS.DICT_COL_NAME);
		return c;
	}

	public static Cursor getModifiedDicts(VocardsDS db, long lastBackup) {
		return db.query(
				VocardsDS.DICT_TABLE,
				null,
				VocardsDS.DICT_COL_MODIFIED + ">?",
				new String[] { lastBackup + "" });
	}

	public static long createDictionary(VocardsDS db, String name, Language nativeLang, Language foreignLang,
			Long parentDictId) {
		db.beginTransaction();

		ContentValues val = new ContentValues();
		val.put(VocardsDS.DICT_COL_NAME, name);
		val.put(VocardsDS.DICT_COL_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDS.DICT_COL_FOREIGN_LANG, foreignLang.getId());
		long id = db.insert(VocardsDS.DICT_TABLE, val);

		val = new ContentValues();
		val.put(VocardsDS.HIER_COL_ANCESTOR, id);
		val.put(VocardsDS.HIER_COL_DESCENDANT, id);
		val.put(VocardsDS.HIER_COL_LENGTH, 0);
		db.insert(VocardsDS.HIER_TABLE, val);

		if (parentDictId != null) {
//			db.execSql(INSERT_HIERARCHY_FROM_PARENT, new String[] { id + "", parentDictId + "" });
			try {
				setAsChildOf(db, id, parentDictId);
			} catch (VocardsException ex) {
				Log.e("dict creation", "setAsChildOf method error.", ex);
			}
		}
		
//		setModified(db, id);
		DBUtil.dictModif(db, id);

		db.setTransactionSuccessful();
		db.endTransaction();
		return id;
	}

	public static void setModified(VocardsDS db, long dictId) {
		ContentValues val = new ContentValues();
		val.put(VocardsDS.DICT_COL_MODIFIED, System.currentTimeMillis());
		db.update(
				VocardsDS.DICT_TABLE,
				val,
				VocardsDS.DICT_COL_ID + "=?",
				new String[] { dictId + "" });
	}

	/**
	 * 
	 * @param db
	 * @param dictId
	 * @return
	 */
	public static int deleteDict(VocardsDS db, long dictId, boolean descendants) {
		int res = 0;

		db.beginTransaction();
		if (descendants) {
			Cursor descs = getDescendantDictionaries(db, dictId);
			descs.moveToFirst();
			while (!descs.isAfterLast()) {
				long descId = descs.getLong(descs.getColumnIndex(VocardsDS.DICT_COL_ID));
				deleteDict(db, descId, false);
				descs.moveToNext();
			}
			descs.close();
		}
		BackupDS.setDeleted(db, dictId);

		db.execSql(UPDATE_DICT_DESCENDANTS_HIERARCHY, new String[] { dictId + "" });
		db.execSql(REMOVE_DICT_HIERARCHY, new String[] { dictId + "", dictId + "" });

		res += WordDS.removeCardsByDict(db, dictId);
		res += db.delete(VocardsDS.DICT_TABLE, dictId);
		db.setTransactionSuccessful();
		db.endTransaction();

		return res;
	}

	public static int updateDictionary(VocardsDS db, long id, String name, Language nativeLang, Language foreignLang) {
		db.beginTransaction();
		
		ContentValues val = new ContentValues();
		val.put(VocardsDS.DICT_COL_NAME, name);
		val.put(VocardsDS.DICT_COL_NATIVE_LANG, nativeLang.getId());
		val.put(VocardsDS.DICT_COL_FOREIGN_LANG, foreignLang.getId());
		int res = db.update(VocardsDS.DICT_TABLE, val, VocardsDS.DICT_COL_ID + "=?", new String[] { id + "" });
		
//		setModified(db, id);
		DBUtil.dictModif(db, id);
		
		db.setTransactionSuccessful();
		db.endTransaction();
		return res;
	}

	public static int setAsRoot(VocardsDS db, long id) {
		String where = VocardsDS.HIER_COL_DESCENDANT + " IN (" +
				QUERY_DICT_DESCENDANT_OR_SELF_IDS +
				") " +
				"AND " + VocardsDS.HIER_COL_ANCESTOR + " IN (" +
				QUERY_ANCESTOR_DICT_ID +
				")";

		db.beginTransaction();
		int deleted = db.delete(
				VocardsDS.HIER_TABLE,
				where,
				new String[] { id + "", id + "" }
				);
//		setModified(db, id);
		DBUtil.dictModif(db, id);
		db.setTransactionSuccessful();
		db.endTransaction();
		return deleted;
	}

	public static void setAsChildOf(VocardsDS db, long movedDictId, long parentDictId) 
			throws ParentIsTheSameException, ParentIsDescendantException {
		db.beginTransaction();

		//Verify that moved and parent dictionaries are not the same!
		if (movedDictId == parentDictId) {
			throw new ParentIsTheSameException("Parent and moved dictionaries are the same!");
		}
		
		//Verify that parent dictionary is not a descendant of moved dictionary!
		Cursor descs = getDescendantDictionaries(db, movedDictId);
		descs.moveToFirst();
		while (!descs.isAfterLast()) {
			long descId = descs.getLong(descs.getColumnIndex(VocardsDS.DICT_COL_ID));
			if (descId == parentDictId) {
				descs.close();
				throw new ParentIsDescendantException("Parent is a descendant of moved dictionary!");
			}
			descs.moveToNext();
		}
		descs.close();

		setAsRoot(db, movedDictId);
		db.execSql(INSERT_HIERARCHY_TO_BE_CHILD_OF, new String[] { movedDictId + "", parentDictId + "" });
		
//		setModified(db, movedDictId);
		DBUtil.dictModif(db, movedDictId);
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public static Cursor getHierarchies(VocardsDS db) {
		return db.query(
				VocardsDS.HIER_TABLE, 
				null, 
				null,
				null);
	}
	
	public static void createHierarchy(VocardsDS db, long ancestor, long descendant, int length) {
		db.beginTransaction();
		
		ContentValues val = new ContentValues();
		val.put(VocardsDS.HIER_COL_ANCESTOR, ancestor);
		val.put(VocardsDS.HIER_COL_DESCENDANT, descendant);
		val.put(VocardsDS.HIER_COL_LENGTH, length);
		db.insert(VocardsDS.HIER_TABLE, val);
		
		db.setTransactionSuccessful();
		db.endTransaction();
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
