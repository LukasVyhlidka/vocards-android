package cz.cvut.fit.vyhliluk.vocards.util.ds;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;

public class WordDS {
	// ================= STATIC ATTRIBUTES ======================

	public static final String NATIVE_WORD = "native_word";
	public static final String FOREIGN_WORD = "foreign_word";

	private static final String QUERY_WORDS = "SELECT " +
			VocardsDS.CARD_COL_ID + ", " +
			VocardsDS.CARD_COL_FACTOR + ", " +
			VocardsDS.CARD_COL_DICTIONARY + ", " +
			"replace(" + VocardsDS.CARD_COL_NATIVE + ", '" + VocardsDS.WORD_DELIM + "', ', ') as " + NATIVE_WORD + ", " +
			"replace(" + VocardsDS.CARD_COL_FOREIGN + ", '" + VocardsDS.WORD_DELIM + "', ', ') as " + FOREIGN_WORD + " " +
			"FROM " + VocardsDS.CARD_TABLE + " " +
			"WHERE " + VocardsDS.CARD_COL_DICTIONARY + " IN (" +
			" SELECT " + VocardsDS.HIER_COL_DESCENDANT + " " +
			"FROM " + VocardsDS.HIER_TABLE + " " +
			"WHERE " + VocardsDS.HIER_COL_ANCESTOR + "=?" +
			")";

	private static final String QUERY_WORDS_ORD = QUERY_WORDS +
			" ORDER BY ";

	private static final String QUERY_WORDS_FILTER = QUERY_WORDS +
			" AND " +
			"(" +
			VocardsDS.CARD_COL_NATIVE + " like ? OR " +
			VocardsDS.CARD_COL_FOREIGN + " like ?" +
			")";

	private static final String QUERY_RANDOM_WORD_UNIFORM = QUERY_WORDS +
			" ORDER by RANDOM()" +
			" LIMIT 1";

	private static final String QUERY_RANDOM_WORD_EXPONENTIAL = QUERY_WORDS +
			" ORDER BY " + VocardsDS.CARD_COL_FACTOR + ", RANDOM()" +
			" LIMIT 1 OFFSET ?";

	private static final String QUERY_WORD_BY_ID = QUERY_WORDS +
			" AND " + VocardsDS.CARD_COL_ID + "=?";

	private static final double LAMBDA = 0.5;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static Cursor getWordsByDictId(VocardsDS db, long id) {
		return db.rawQuery(QUERY_WORDS, new String[] { id + "" });
	}

	public static Cursor getOrdWordsByDictId(VocardsDS db, long id) {
		return getOrdWordsByDictId(db, id, "lower(" + VocardsDS.CARD_COL_NATIVE + ")");
	}

	public static Cursor getOrdWordsByDictId(VocardsDS db, long id, String orderBy) {
		return db.rawQuery(QUERY_WORDS_ORD + orderBy, new String[] { id + "" });
	}

	public static Cursor getWordsByDictIdFilter(VocardsDS db, long id, String filter) {
		String f = "%" + filter + "%";
		return db.rawQuery(QUERY_WORDS_FILTER, new String[] { id + "", f, f });
	}

	public static Cursor getRandomWord(VocardsDS db, long dictId) {
		double rand = Math.random();
		if (rand > 0.8) {
			return getExpRandomWord(db, dictId);
		} else {
			return getUnifRandomWord(db, dictId);
		}
	}

	public static Cursor getUnifRandomWord(VocardsDS db, long dictId) {
		return db.rawQuery(QUERY_RANDOM_WORD_UNIFORM, new String[] { dictId + "" });
	}

	public static Cursor getExpRandomWord(VocardsDS db, long dictId) {
		int wordCount = DictionaryDS.getWordCount(db, dictId);
		int rand = getExponentialRandom(wordCount - 1, LAMBDA);
		return db.rawQuery(QUERY_RANDOM_WORD_EXPONENTIAL, new String[] { dictId + "", rand + "" });
	}

	public static Cursor getWordById(VocardsDS db, long dictId, long cardId) {
		return db.rawQuery(QUERY_WORD_BY_ID, new String[] { dictId + "", cardId + "" });
	}

	public static Cursor getCardById(VocardsDS db, long cardId) {
		return db.query(
				VocardsDS.CARD_TABLE,
				null,
				VocardsDS.CARD_COL_ID + "=?",
				new String[] { cardId + "" });
	}

	public static long createCard(VocardsDS db, List<String> natWords, List<String> forWords, long dictId) {
		return createCard(db, natWords, forWords, CardUtil.MIN_FACTOR, dictId);
	}

	public static long createCard(VocardsDS db, List<String> natWords, List<String> forWords, int factor, long dictId) {
		db.beginTransaction();

		ContentValues cardValues = new ContentValues();
		cardValues.put(VocardsDS.CARD_COL_DICTIONARY, dictId);
		cardValues.put(VocardsDS.CARD_COL_FACTOR, factor);
		cardValues.put(VocardsDS.CARD_COL_NATIVE, CardUtil.implodeWords(natWords));
		cardValues.put(VocardsDS.CARD_COL_FOREIGN, CardUtil.implodeWords(forWords));
		long cardId = db.insert(VocardsDS.CARD_TABLE, cardValues);

		DBUtil.dictModif(db, dictId);

		if (cardId != -1) {
			db.setTransactionSuccessful();
		}
		db.endTransaction();
		return cardId;
	}

	public static int removeCard(VocardsDS db, long cardId) {
		db.beginTransaction();

		int res = 0;
		res += db.delete(VocardsDS.CARD_TABLE, cardId);

		db.setTransactionSuccessful();
		db.endTransaction();
		return res;
	}

	public static int removeCardsByDict(VocardsDS db, long dictId) {
		db.beginTransaction();

		int res = 0;
		Cursor c = db.query(VocardsDS.CARD_TABLE, null, VocardsDS.CARD_COL_DICTIONARY + "=?", new String[] { dictId + "" });
		c.moveToNext();
		while (!c.isAfterLast()) {
			res += removeCard(db, c.getLong(c.getColumnIndex(VocardsDS.CARD_COL_ID)));
			c.moveToNext();
		}
		c.close();

		db.setTransactionSuccessful();
		db.endTransaction();
		return res;
	}

	public static void updateFactor(VocardsDS db, long cardId, int factor) {
		ContentValues val = new ContentValues();
		val.put(VocardsDS.CARD_COL_FACTOR, factor);

		db.update(VocardsDS.CARD_TABLE, val, VocardsDS.CARD_COL_ID + "=?", new String[] { cardId + "" });
	}

	public static void updateCard(VocardsDS db, long cardId, List<String> natWords, List<String> forWords) {
		db.beginTransaction();

		ContentValues val = new ContentValues();
		val.put(VocardsDS.CARD_COL_FACTOR, CardUtil.MIN_FACTOR);
		val.put(VocardsDS.CARD_COL_NATIVE, CardUtil.implodeWords(natWords));
		val.put(VocardsDS.CARD_COL_FOREIGN, CardUtil.implodeWords(forWords));

		db.update(VocardsDS.CARD_TABLE, val, VocardsDS.CARD_COL_ID + "=?", new String[] { cardId + "" });

		// Get card dictionary because of set modified
		Cursor card = getCardById(db, cardId);
		card.moveToFirst();
		long dictId = card.getLong(card.getColumnIndex(VocardsDS.CARD_COL_DICTIONARY));
		card.close();
		DBUtil.dictModif(db, dictId);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public static void moveCards(VocardsDS db, List<Long> cardIds, long dictId) {
		db.beginTransaction();

		ContentValues val = new ContentValues();
		val.put(VocardsDS.CARD_COL_DICTIONARY, dictId);
		for (Long id : cardIds) {
			db.update(VocardsDS.CARD_TABLE, val, VocardsDS.CARD_COL_ID + "=?", new String[] { id + "" });
		}

		DBUtil.dictModif(db, dictId);

		db.setTransactionSuccessful();
		db.endTransaction();
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
