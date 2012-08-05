package cz.cvut.fit.vyhliluk.vocards.util.ds;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;

public class WordDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String NATIVE_WORD = "native_word";
	public static final String FOREIGN_WORD = "foreign_word";

	private static final String QUERY_WORDS = "SELECT " +
			VocardsDataSource.CARD_COLUMN_ID + ", " +
			VocardsDataSource.CARD_COLUMN_FACTOR + ", " +
			VocardsDataSource.CARD_COLUMN_DICTIONARY + ", " +
			"replace(" + VocardsDataSource.CARD_COLUMN_NATIVE + ", '" + VocardsDataSource.WORD_DELIM + "', ', ') as " + NATIVE_WORD + ", " +
			"replace(" + VocardsDataSource.CARD_COLUMN_FOREIGN + ", '" + VocardsDataSource.WORD_DELIM + "', ', ') as " + FOREIGN_WORD + " " +
			"FROM " + VocardsDataSource.CARD_TABLE + " " +
			"WHERE " + VocardsDataSource.CARD_COLUMN_DICTIONARY + " IN (" +
			" SELECT " + VocardsDataSource.HIERARCHY_COLUMN_DESCENDANT + " " +
			"FROM " + VocardsDataSource.HIERARCHY_TABLE + " " +
			"WHERE " + VocardsDataSource.HIERARCHY_COLUMN_ANCESTOR +"=?" +
			")";

	private static final String QUERY_WORDS_ORD = QUERY_WORDS +
			" ORDER BY ";

	private static final String QUERY_WORDS_FILTER = QUERY_WORDS +
			" AND " +
			"(" +
			VocardsDataSource.CARD_COLUMN_NATIVE + " like ? OR " +
			VocardsDataSource.CARD_COLUMN_FOREIGN + " like ?" +
			")";

	private static final String QUERY_RANDOM_WORD_UNIFORM = QUERY_WORDS +
			" ORDER by RANDOM()" +
			" LIMIT 1";

	private static final String QUERY_RANDOM_WORD_EXPONENTIAL = QUERY_WORDS +
			" ORDER BY " + VocardsDataSource.CARD_COLUMN_FACTOR + ", RANDOM()" +
			" LIMIT 1 OFFSET ?";

	private static final String QUERY_WORD_BY_ID = QUERY_WORDS +
			" AND " + VocardsDataSource.CARD_COLUMN_ID + "=?";

	private static final double LAMBDA = 0.5;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getWordsByDictId(VocardsDataSource db, long id) {
		return db.rawQuery(QUERY_WORDS, new String[] { id + "" });
	}

	public static Cursor getOrdWordsByDictId(VocardsDataSource db, long id) {
		return getOrdWordsByDictId(db, id, "lower("+VocardsDataSource.CARD_COLUMN_NATIVE +")");
	}

	public static Cursor getOrdWordsByDictId(VocardsDataSource db, long id, String orderBy) {
		return db.rawQuery(QUERY_WORDS_ORD + orderBy, new String[] { id + "" });
	}

	public static Cursor getWordsByDictIdFilter(VocardsDataSource db, long id, String filter) {
		String f = "%" + filter + "%";
		return db.rawQuery(QUERY_WORDS_FILTER, new String[] { id + "", f, f });
	}

	public static Cursor getRandomWord(VocardsDataSource db, long dictId) {
		double rand = Math.random();
		if (rand > 0.8) {
			return getExpRandomWord(db, dictId);
		} else {
			return getUnifRandomWord(db, dictId);
		}
	}

	public static Cursor getUnifRandomWord(VocardsDataSource db, long dictId) {
		return db.rawQuery(QUERY_RANDOM_WORD_UNIFORM, new String[] { dictId + "" });
	}

	public static Cursor getExpRandomWord(VocardsDataSource db, long dictId) {
		int wordCount = DictionaryDS.getWordCount(db, dictId);
		int rand = getExponentialRandom(wordCount - 1, LAMBDA);
		return db.rawQuery(QUERY_RANDOM_WORD_EXPONENTIAL, new String[] { dictId + "", rand + "" });
	}

	public static Cursor getWordById(VocardsDataSource db, long dictId, long cardId) {
		return db.rawQuery(QUERY_WORD_BY_ID, new String[] { dictId + "", cardId + "" });
	}

	public static Cursor getCardById(VocardsDataSource db, long cardId) {
		return db.query(
				VocardsDataSource.CARD_TABLE,
				null,
				VocardsDataSource.CARD_COLUMN_ID + "=?",
				new String[] { cardId + "" });
	}

	public static long createCard(VocardsDataSource db, List<String> natWords, List<String> forWords, long dictId) {
		return createCard(db, natWords, forWords, CardUtil.MIN_FACTOR, dictId);
	}

	public static long createCard(VocardsDataSource db, List<String> natWords, List<String> forWords, int factor, long dictId) {
		db.begin();

		ContentValues cardValues = new ContentValues();
		cardValues.put(VocardsDataSource.CARD_COLUMN_DICTIONARY, dictId);
		cardValues.put(VocardsDataSource.CARD_COLUMN_FACTOR, factor);
		cardValues.put(VocardsDataSource.CARD_COLUMN_NATIVE, CardUtil.implodeWords(natWords));
		cardValues.put(VocardsDataSource.CARD_COLUMN_FOREIGN, CardUtil.implodeWords(forWords));
		long cardId = db.insert(VocardsDataSource.CARD_TABLE, cardValues);

		if (cardId != -1) {
			db.commit();
			return cardId;
		} else {
			db.rollback();
			return -1;
		}
	}

	public static int removeCard(VocardsDataSource db, long cardId) {
		db.begin();

		int res = 0;
		res += db.delete(VocardsDataSource.CARD_TABLE, cardId);

		db.commit();

		return res;
	}

	public static int removeCardsByDict(VocardsDataSource db, long dictId) {
		db.begin();

		int res = 0;
		Cursor c = db.query(VocardsDataSource.CARD_TABLE, null, VocardsDataSource.CARD_COLUMN_DICTIONARY + "=?", new String[] { dictId + "" });
		c.moveToNext();
		while (!c.isAfterLast()) {
			res += removeCard(db, c.getLong(c.getColumnIndex(VocardsDataSource.CARD_COLUMN_ID)));
			c.moveToNext();
		}

		c.close();

		db.commit();
		return res;
	}

	public static void updateFactor(VocardsDataSource db, long cardId, int factor) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.CARD_COLUMN_FACTOR, factor);

		db.update(VocardsDataSource.CARD_TABLE, val, VocardsDataSource.CARD_COLUMN_ID + "=?", new String[] { cardId + "" });
	}

	public static void updateCard(VocardsDataSource db, long cardId, List<String> natWords, List<String> forWords) {
		db.begin();

		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.CARD_COLUMN_FACTOR, CardUtil.MIN_FACTOR);
		val.put(VocardsDataSource.CARD_COLUMN_NATIVE, CardUtil.implodeWords(natWords));
		val.put(VocardsDataSource.CARD_COLUMN_FOREIGN, CardUtil.implodeWords(forWords));

		db.update(VocardsDataSource.CARD_TABLE, val, VocardsDataSource.CARD_COLUMN_ID + "=?", new String[] { cardId + "" });

		db.commit();
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private static int getExponentialRandom(int max, double lambda) {
		double rand = Math.random();
		double expRand = (Math.log(1 - rand) / (-lambda * Math.log(Math.E)));
		int round = ((int) expRand) % (max + 1);
		Log.d("WordDS - exponential random", expRand + " (" + round + ")");
		return round;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
