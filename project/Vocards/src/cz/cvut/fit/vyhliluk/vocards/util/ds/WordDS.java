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
			"c." + VocardsDataSource.CARD_COLUMN_ID + ", " +
			"nat." + VocardsDataSource.WORD_COLUMN_TEXT + " as " + NATIVE_WORD + ", " +
			"for." + VocardsDataSource.WORD_COLUMN_TEXT + " as " + FOREIGN_WORD + ", " +
			"c." + VocardsDataSource.CARD_COLUMN_FACTOR + " " +
			"FROM " +
			VocardsDataSource.CARD_TABLE + " c, " +
			"(" +
			"SELECT " +
			VocardsDataSource.WORD_COLUMN_ID + ", " +
			"group_concat(" + VocardsDataSource.WORD_COLUMN_TEXT + ", ', ') as " + VocardsDataSource.WORD_COLUMN_TEXT + ", " +
			VocardsDataSource.WORD_COLUMN_CARD + ", " +
			VocardsDataSource.WORD_COLUMN_TYPE + " " +
			"FROM " + VocardsDataSource.WORD_TABLE + " " +
			"WHERE " + VocardsDataSource.WORD_COLUMN_TYPE + "=" + VocardsDataSource.WORD_TYPE_NATIVE + " " +
			"GROUP BY " + VocardsDataSource.WORD_COLUMN_CARD +
			") nat, " +
			"(" +
			"SELECT " +
			VocardsDataSource.WORD_COLUMN_ID + ", " +
			"group_concat(" + VocardsDataSource.WORD_COLUMN_TEXT + ", ', ') as " + VocardsDataSource.WORD_COLUMN_TEXT + ", " +
			VocardsDataSource.WORD_COLUMN_CARD + ", " +
			VocardsDataSource.WORD_COLUMN_TYPE + " " +
			"FROM " + VocardsDataSource.WORD_TABLE + " " +
			"WHERE " + VocardsDataSource.WORD_COLUMN_TYPE + "=" + VocardsDataSource.WORD_TYPE_FOREIGN + " " +
			"GROUP BY " + VocardsDataSource.WORD_COLUMN_CARD +
			") for " +
			"WHERE " +
			VocardsDataSource.CARD_COLUMN_DICTIONARY + "=? AND " +
			"nat." + VocardsDataSource.WORD_COLUMN_CARD + "=" + "c." + VocardsDataSource.CARD_COLUMN_ID + " AND " +
			"for." + VocardsDataSource.WORD_COLUMN_CARD + "=" + "c." + VocardsDataSource.CARD_COLUMN_ID + " AND " +
			"nat." + VocardsDataSource.WORD_COLUMN_TYPE + "=" + VocardsDataSource.WORD_TYPE_NATIVE + " AND " +
			"for." + VocardsDataSource.WORD_COLUMN_TYPE + "=" + VocardsDataSource.WORD_TYPE_FOREIGN;

	private static final String QUERY_WORDS_ORD = QUERY_WORDS +
			" ORDER BY nat." + VocardsDataSource.WORD_COLUMN_TEXT;

	private static final String QUERY_WORDS_FILTER = QUERY_WORDS +
			" AND " +
			"(" +
			"nat." + VocardsDataSource.WORD_COLUMN_TEXT + " like ? OR " +
			"for." + VocardsDataSource.WORD_COLUMN_TEXT + " like ?" +
			")";

	private static final String QUERY_RANDOM_WORD_UNIFORM = QUERY_WORDS +
			" ORDER by RANDOM()" +
			" LIMIT 1";

	private static final String QUERY_RANDOM_WORD_EXPONENTIAL = QUERY_WORDS +
			" ORDER BY c." + VocardsDataSource.CARD_COLUMN_FACTOR + ", RANDOM()" +
			" LIMIT 1 OFFSET ?";

	private static final String QUERY_WORD_BY_ID = QUERY_WORDS +
			" AND c." + VocardsDataSource.CARD_COLUMN_ID + "=?";

	private static final double LAMBDA = 0.5;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getWordsByDictId(VocardsDataSource db, long id) {
		return db.rawQuery(QUERY_WORDS, new String[] { id + "" });
	}

	public static Cursor getOrdWordsByDictId(VocardsDataSource db, long id) {
		return db.rawQuery(QUERY_WORDS_ORD, new String[] { id + "" });
	}

	public static Cursor getWordsByDictIdFilter(VocardsDataSource db, long id, String filter) {
		String f = "%"+filter+"%";
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

	public static Cursor getCardNativeWords(VocardsDataSource db, long cardId) {
		return db.query(
				VocardsDataSource.WORD_TABLE,
				null,
				VocardsDataSource.WORD_COLUMN_CARD + "=? AND " + VocardsDataSource.WORD_COLUMN_TYPE + "=?",
				new String[] { cardId + "", VocardsDataSource.WORD_TYPE_NATIVE + "" });
	}

	public static Cursor getCardForeignWords(VocardsDataSource db, long cardId) {
		return db.query(
				VocardsDataSource.WORD_TABLE,
				null,
				VocardsDataSource.WORD_COLUMN_CARD + "=? AND " + VocardsDataSource.WORD_COLUMN_TYPE + "=?",
				new String[] { cardId + "", VocardsDataSource.WORD_TYPE_FOREIGN + "" });
	}

	public static long createCard(VocardsDataSource db, List<String> natWords, List<String> forWords, long dictId) {
		return createCard(db, natWords, forWords, CardUtil.MIN_FACTOR, dictId);
	}

	public static long createCard(VocardsDataSource db, List<String> natWords, List<String> forWords, int factor, long dictId) {
		db.begin();

		ContentValues cardValues = new ContentValues();
		cardValues.put(VocardsDataSource.CARD_COLUMN_DICTIONARY, dictId);
		cardValues.put(VocardsDataSource.CARD_COLUMN_FACTOR, factor);
		long cardId = db.insert(VocardsDataSource.CARD_TABLE, cardValues);

		insertWords(db, forWords, cardId, VocardsDataSource.WORD_TYPE_FOREIGN);
		insertWords(db, natWords, cardId, VocardsDataSource.WORD_TYPE_NATIVE);

		if (cardId != -1) {
			db.commit();
//			Log.d("OK", "OK");
			return cardId;
		} else {
			db.rollback();
//			Log.d("ROLBACK", "ROLBACK");
			return -1;
		}
	}

	public static int removeCard(VocardsDataSource db, long cardId) {
		db.begin();

		int res = 0;
		res += removeWords(db, cardId);
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

		removeWords(db, cardId);
		insertWords(db, natWords, cardId, VocardsDataSource.WORD_TYPE_NATIVE);
		insertWords(db, forWords, cardId, VocardsDataSource.WORD_TYPE_FOREIGN);
		updateFactor(db, cardId, CardUtil.MIN_FACTOR);

		db.commit();
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private static void insertWords(VocardsDataSource db, List<String> words, long cardId, int wordType) {
		db.begin();
		for (String forWord : words) {
			ContentValues forWordVal = new ContentValues();
			forWordVal.put(VocardsDataSource.WORD_COLUMN_CARD, cardId);
			forWordVal.put(VocardsDataSource.WORD_COLUMN_TYPE, wordType);
			forWordVal.put(VocardsDataSource.WORD_COLUMN_TEXT, forWord);
			db.insert(VocardsDataSource.WORD_TABLE, forWordVal);
		}
		db.commit();
	}

	private static int removeWords(VocardsDataSource db, long cardId) {
		return db.delete(VocardsDataSource.WORD_TABLE, VocardsDataSource.WORD_COLUMN_CARD + "=?", new String[] { cardId + "" });
	}

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
