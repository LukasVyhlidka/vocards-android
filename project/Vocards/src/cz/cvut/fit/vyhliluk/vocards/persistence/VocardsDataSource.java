package cz.cvut.fit.vyhliluk.vocards.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VocardsDataSource {
	// ================= STATIC ATTRIBUTES ======================

	public static final String DB_NAME = "vocards";
	public static final int DB_VERSION = 1;

	public static final String DICTIONARY_TABLE = "dictionary";
	public static final String DICTIONARY_COLUMN_ID = "_id";
	public static final String DICTIONARY_COLUMN_NAME = "name";
	public static final String DICTIONARY_COLUMN_NATIVE_LANG = "native_lang";
	public static final String DICTIONARY_COLUMN_FOREIGN_LANG = "foreign_lang";

	public static final String CARD_TABLE = "card";
	public static final String CARD_COLUMN_ID = "_id";
	public static final String CARD_COLUMN_FACTOR = "factor";
	public static final String CARD_COLUMN_DICTIONARY = "dict_id";

	public static final String WORD_TABLE = "word";
	public static final String WORD_COLUMN_ID = "_id";
	public static final String WORD_COLUMN_TEXT = "text";
	public static final String WORD_COLUMN_CARD = "card_id";

	private static final String CREATE_DICTIONARY = "CREATE TABLE " + DICTIONARY_TABLE + "("
			+ DICTIONARY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ DICTIONARY_COLUMN_NAME + " TEXT NOT NULL,"
			+ DICTIONARY_COLUMN_NATIVE_LANG + " INTEGER NOT NULL,"
			+ DICTIONARY_COLUMN_FOREIGN_LANG + " INTEGER NOT NULL"
			+ ");";

	private static final String CREATE_CARD = "CREATE TABLE " + CARD_TABLE + "("
			+ CARD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CARD_COLUMN_FACTOR + " INTEGER NOT NULL,"
			+ CARD_COLUMN_DICTIONARY + " INTEGER NOT NULL,"
			+ "FOREIGN KEY (" + CARD_COLUMN_DICTIONARY + ") REFERENCES " + DICTIONARY_TABLE + "(" + DICTIONARY_COLUMN_ID + ")"
			+ ");";

	private static final String CREATE_WORD = "CREATE TABLE " + WORD_TABLE + "("
			+ WORD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ WORD_COLUMN_TEXT + " TEXT,"
			+ WORD_COLUMN_CARD + " INTEGER NOT NULL,"
			+ "FOREIGN KEY (" + WORD_COLUMN_CARD + ") REFERENCES " + CARD_TABLE + "(" + CARD_COLUMN_ID + ")"
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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int from, int to) {
		}

	}

}
