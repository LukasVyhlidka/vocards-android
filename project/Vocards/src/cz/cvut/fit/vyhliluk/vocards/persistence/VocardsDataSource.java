package cz.cvut.fit.vyhliluk.vocards.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VocardsDataSource {
	// ================= STATIC ATTRIBUTES ======================

	public static final String DB_NAME = "vocards";
	public static final int DB_VERSION = 2;

	public static final String DB_DEFAULT_ID = "_id";

	public static final String DICTIONARY_TABLE = "dictionary";
	public static final String DICTIONARY_COLUMN_ID = DB_DEFAULT_ID;
	public static final String DICTIONARY_COLUMN_NAME = "name";
	public static final String DICTIONARY_COLUMN_NATIVE_LANG = "native_lang";
	public static final String DICTIONARY_COLUMN_FOREIGN_LANG = "foreign_lang";
	public static final String DICTIONARY_COLUMN_MODIFIED = "modified";

	public static final String CARD_TABLE = "card";
	public static final String CARD_COLUMN_ID = DB_DEFAULT_ID;
	public static final String CARD_COLUMN_FACTOR = "factor";
	public static final String CARD_COLUMN_DICTIONARY = "dict_id";
	public static final String CARD_COLUMN_NATIVE = "native_word";
	public static final String CARD_COLUMN_FOREIGN = "foreign_word";

	public static final String BACKUP_TABLE = "backup";
	public static final String BACKUP_COLUMN_ID = DB_DEFAULT_ID;
	public static final String BACKUP_COLUMN_DICTIONARY = "dict_id";
	public static final String BACKUP_COLUMN_STATE = "state";
	
	public static final String HIERARCHY_TABLE = "hierarchy";
	public static final String HIERARCHY_COLUMN_ANCESTOR = "ancestor";
	public static final String HIERARCHY_COLUMN_DESCENDANT = "descendant";
	public static final String HIERARCHY_COLUMN_LENGTH = "length";
	
	public static final int BACKUP_STATE_BACKUPED = 1;
	public static final int BACKUP_STATE_DELETED = 2;
	
	public static final String WORD_DELIM = "|||";

	private static final String CREATE_DICTIONARY = "CREATE TABLE " + DICTIONARY_TABLE + "("
			+ DICTIONARY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ DICTIONARY_COLUMN_NAME + " TEXT NOT NULL,"
			+ DICTIONARY_COLUMN_NATIVE_LANG + " INTEGER NOT NULL,"
			+ DICTIONARY_COLUMN_FOREIGN_LANG + " INTEGER NOT NULL,"
			+ DICTIONARY_COLUMN_MODIFIED +" INTEGER"
			+ ");";

	private static final String CREATE_CARD = "CREATE TABLE " + CARD_TABLE + "("
			+ CARD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CARD_COLUMN_FACTOR + " INTEGER NOT NULL,"
			+ CARD_COLUMN_DICTIONARY + " INTEGER NOT NULL,"
			+ CARD_COLUMN_NATIVE +" TEXT NOT NULL,"
			+ CARD_COLUMN_FOREIGN +" TEXT NOT NULL, "
			+ "FOREIGN KEY (" + CARD_COLUMN_DICTIONARY + ") REFERENCES " + DICTIONARY_TABLE + "(" + DICTIONARY_COLUMN_ID + ")"
			+ ");";

	private static final String CREATE_BACKUP = "CREATE TABLE " + BACKUP_TABLE + "("
			+ BACKUP_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ BACKUP_COLUMN_DICTIONARY + " INTEGER,"
			+ BACKUP_COLUMN_STATE +" INTEGER NOT NULL,"
			+ "FOREIGN KEY ("+ BACKUP_COLUMN_DICTIONARY +") REFERENCES "+ DICTIONARY_TABLE +"("+ DICTIONARY_COLUMN_ID + ")"
			+ ")";
	
	private static final String CREATE_HIERARCHY = "CREATE TABLE "+ HIERARCHY_TABLE +"("
			+ HIERARCHY_COLUMN_ANCESTOR +" INTEGER NOT NULL,"
			+ HIERARCHY_COLUMN_DESCENDANT +" INTEGER NOT NULL,"
			+ HIERARCHY_COLUMN_LENGTH +" INTEGER NOT NULL,"
			+"FOREIGN KEY ("+ HIERARCHY_COLUMN_ANCESTOR +") REFERENCES "+ DICTIONARY_TABLE +"("+ DICTIONARY_COLUMN_ID +"),"
			+"FOREIGN KEY ("+ HIERARCHY_COLUMN_DESCENDANT +") REFERENCES "+ DICTIONARY_TABLE +"("+ DICTIONARY_COLUMN_ID +")"
			+ ")";

	// ================= INSTANCE ATTRIBUTES ====================

	private SQLiteOpenHelper helper = null;
	private SQLiteDatabase db = null;

	// ================= CONSTRUCTORS ===========================

	public VocardsDataSource(Context ctx) {
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
		return this.db.query(table, null, VocardsDataSource.DB_DEFAULT_ID + "=?", new String[] { id + "" }, null, null, null);
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

	public void begin() {
		this.db.beginTransaction();
	}

	public void commit() {
		this.db.setTransactionSuccessful();
		this.db.endTransaction();
	}

	public void rollback() {
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
			db.execSQL(CREATE_BACKUP);
			db.execSQL(CREATE_HIERARCHY);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int from, int to) {
			if (from < 2) {
				db.execSQL(CREATE_HIERARCHY);
				db.beginTransaction();
				Cursor c = db.query(DICTIONARY_TABLE, null, null, null, null, null, null);
				while (!c.isLast()) {
					c.moveToNext();
					long id = c.getLong(c.getColumnIndex(DICTIONARY_COLUMN_ID));
					ContentValues val = new ContentValues();
					val.put(HIERARCHY_COLUMN_ANCESTOR, id);
					val.put(HIERARCHY_COLUMN_DESCENDANT, id);
					val.put(HIERARCHY_COLUMN_LENGTH, 0);
					db.insert(HIERARCHY_TABLE, null, val);
				}
				db.setTransactionSuccessful();
				db.endTransaction();
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
