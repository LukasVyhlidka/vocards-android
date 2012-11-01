package cz.cvut.fit.vyhliluk.vocards.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VocardsDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String DB_NAME = "vocards";
	public static final int DB_VERSION = 4;

	public static final String DB_DEFAULT_ID = "_id";

	public static final String DICT_TABLE = "dictionary";
	public static final String DICT_COL_ID = DB_DEFAULT_ID;
	public static final String DICT_COL_NAME = "name";
	public static final String DICT_COL_NATIVE_LANG = "native_lang";
	public static final String DICT_COL_FOREIGN_LANG = "foreign_lang";
	public static final String DICT_COL_MODIFIED = "modified";

	public static final String CARD_TABLE = "card";
	public static final String CARD_COL_ID = DB_DEFAULT_ID;
	public static final String CARD_COL_FACTOR = "factor";
	public static final String CARD_COL_DICTIONARY = "dict_id";
	public static final String CARD_COL_NATIVE = "native_word";
	public static final String CARD_COL_FOREIGN = "foreign_word";

	public static final String BACKUP_TABLE = "backup";
	public static final String BACKUP_COL_ID = DB_DEFAULT_ID;
	public static final String BACKUP_COL_DICTIONARY = "dict_id";
	public static final String BACKUP_COL_STATE = "state";

	public static final String HIER_TABLE = "hierarchy";
	public static final String HIER_COL_ANCESTOR = "ancestor";
	public static final String HIER_COL_DESCENDANT = "descendant";
	public static final String HIER_COL_LENGTH = "length";

	public static final String WORD_TABLE = "word";
	public static final String WORD_COL_ID = DB_DEFAULT_ID;
	public static final String WORD_COL_TYPE = "type";
	public static final String WORD_COL_CARD_ID = "cardId";
	public static final String WORD_COL_WORD = "word";

	public static final int BACKUP_STATE_BACKUPED = 1;
	public static final int BACKUP_STATE_DELETED = 2;

	public static final String WORD_DELIM = "|||";

	public static final int WORD_TYPE_NAT = 1;
	public static final int WORD_TYPE_FOR = 2;

	static final String CREATE_DICTIONARY = "CREATE TABLE " + DICT_TABLE + "("
			+ DICT_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ DICT_COL_NAME + " TEXT NOT NULL,"
			+ DICT_COL_NATIVE_LANG + " INTEGER NOT NULL,"
			+ DICT_COL_FOREIGN_LANG + " INTEGER NOT NULL,"
			+ DICT_COL_MODIFIED + " INTEGER NOT NULL DEFAULT 0"
			+ ");";

	static final String CREATE_CARD = "CREATE TABLE " + CARD_TABLE + "("
			+ CARD_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CARD_COL_FACTOR + " INTEGER NOT NULL,"
			+ CARD_COL_DICTIONARY + " INTEGER NOT NULL,"
			+ CARD_COL_NATIVE + " TEXT NOT NULL,"
			+ CARD_COL_FOREIGN + " TEXT NOT NULL, "
			+ "FOREIGN KEY (" + CARD_COL_DICTIONARY + ") REFERENCES " + DICT_TABLE + "(" + DICT_COL_ID + ")"
			+ ");";

	static final String CREATE_WORD = "CREATE TABLE " + WORD_TABLE + "("
			+ WORD_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ WORD_COL_CARD_ID + " INTEGER NOT NULL,"
			+ WORD_COL_TYPE + " INTEGER NOT NULL,"
			+ WORD_COL_WORD + " TEXT NOT NULL,"
			+ "FOREIGN KEY (" + WORD_COL_CARD_ID + ") REFERENCES " + CARD_TABLE + "(" + CARD_COL_ID + ")"
			+ ")";

	static final String CREATE_WORD_CARD_ID_INDEX = "CREATE INDEX index_word_card_id ON " + WORD_TABLE + "(" + WORD_COL_CARD_ID + ")";

	static final String CREATE_BACKUP = "CREATE TABLE " + BACKUP_TABLE + "("
			+ BACKUP_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ BACKUP_COL_DICTIONARY + " INTEGER,"
			+ BACKUP_COL_STATE + " INTEGER NOT NULL,"
			+ "FOREIGN KEY (" + BACKUP_COL_DICTIONARY + ") REFERENCES " + DICT_TABLE + "(" + DICT_COL_ID + ")"
			+ ")";

	static final String CREATE_HIERARCHY = "CREATE TABLE " + HIER_TABLE + "("
			+ HIER_COL_ANCESTOR + " INTEGER NOT NULL,"
			+ HIER_COL_DESCENDANT + " INTEGER NOT NULL,"
			+ HIER_COL_LENGTH + " INTEGER NOT NULL,"
			+ "FOREIGN KEY (" + HIER_COL_ANCESTOR + ") REFERENCES " + DICT_TABLE + "(" + DICT_COL_ID + "),"
			+ "FOREIGN KEY (" + HIER_COL_DESCENDANT + ") REFERENCES " + DICT_TABLE + "(" + DICT_COL_ID + ")"
			+ ")";

	// ================= INSTANCE ATTRIBUTES ====================

	private SQLiteOpenHelper helper = null;
	private SQLiteDatabase db = null;

	// ================= CONSTRUCTORS ===========================

	public VocardsDS(Context ctx) {
		helper = new DSOpenHelper(ctx);
	}

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	public void open() {
		this.db = helper.getWritableDatabase();
	}

	public void close() {
		this.db.close();
	}

	public Cursor query(String table, String[] columns, String selection, String[] args) {
		return this.db.query(table, columns, selection, args, null, null, null);
	}

	public Cursor query(String table, String[] columns, String selection, String[] args, String orderBy) {
		return this.db.query(table, columns, selection, args, null, null, orderBy);
	}

	public Cursor queryById(String table, long id) {
		return this.db.query(table, null, VocardsDS.DB_DEFAULT_ID + "=?", new String[] { id + "" }, null, null, null);
	}

	public Cursor query(String table, String[] columns, String selection, String[] args, String orderBy, String limit) {
		return this.db.query(table, columns, selection, args, null, null, orderBy, limit);
	}

	public long insert(String table, ContentValues val) {
		return this.db.insert(table, null, val);
	}

	public int update(String table, ContentValues val, String selection, String[] args) {
		return this.db.update(table, val, selection, args);
	}

	public int delete(String table, String whereClause, String[] whereArgs) {
		return this.db.delete(table, whereClause, whereArgs);
	}

	public Cursor rawQuery(String sql, String[] args) {
		return this.db.rawQuery(sql, args);
	}

	public void execSql(String sql, String[] args) {
		this.db.execSQL(sql, args);
	}

	public void beginTransaction() {
		this.db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		this.db.setTransactionSuccessful();
	}

	public void endTransaction() {
		this.db.endTransaction();
	}

	/**
	 * Works only if table has primary key named "_id"
	 * 
	 * @param table
	 * @param id
	 * @return
	 */
	public int delete(String table, long id) {
		return this.db.delete(table, DB_DEFAULT_ID + "=?", new String[] { id + "" });
	}

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	private class DSOpenHelper extends SQLiteOpenHelper {

		DSOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DICTIONARY);
			db.execSQL(CREATE_CARD);
			db.execSQL(CREATE_WORD);
			db.execSQL(CREATE_BACKUP);
			db.execSQL(CREATE_HIERARCHY);
			
			db.execSQL(CREATE_WORD_CARD_ID_INDEX);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int from, int to) {
			if (from < 2) {
				db.execSQL(CREATE_HIERARCHY);
				db.beginTransaction();
				Cursor c = db.query(DICT_TABLE, null, null, null, null, null, null);
				while (!c.isLast()) {
					c.moveToNext();
					long id = c.getLong(c.getColumnIndex(DICT_COL_ID));
					ContentValues val = new ContentValues();
					val.put(HIER_COL_ANCESTOR, id);
					val.put(HIER_COL_DESCENDANT, id);
					val.put(HIER_COL_LENGTH, 0);
					db.insert(HIER_TABLE, null, val);
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			}

			if (from < 3) {
				db.beginTransaction();

				ContentValues val = new ContentValues();
				val.put(VocardsDS.DICT_COL_MODIFIED, System.currentTimeMillis());
				db.update(
						VocardsDS.DICT_TABLE,
						val,
						VocardsDS.DICT_COL_MODIFIED + " IS NULL",
						null);

				db.setTransactionSuccessful();
				db.endTransaction();
			}

			if (from < 4) {
				VocardsDSMigrator.version4(db);
			}
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			if (!db.isReadOnly()) {
				// Enable foreign key constraints
				db.execSQL("PRAGMA foreign_keys=ON;");
			}
		}

	}

}
