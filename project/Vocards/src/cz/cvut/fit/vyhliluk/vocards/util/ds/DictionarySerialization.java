package cz.cvut.fit.vyhliluk.vocards.util.ds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;

public class DictionarySerialization {
	// ================= STATIC ATTRIBUTES ======================

	public static final String VOCARDS_MIME = "application/vnd.cz.cvut.fit.vyhliluk.vocards";

	public static final String EXPORT_FILE_PREFIX = "vocardsExport";
	public static final String EXPORT_FILE_SUFFIX = ".json";

	public static final String KEY_DICTIONARY_LIST = "dicts";

	public static final String KEY_DICTIONARY_NAME = "dictName";
	public static final String KEY_NATIVE_LANG = "natLang";
	public static final String KEY_FOREIGN_LANG = "forLang";
	public static final String KEY_CARDS = "cards";

	public static final String KEY_FOREIGN_WORD = "for";
	public static final String KEY_NATIVE_WORD = "nat";
	public static final String KEY_CARD_FACTOR = "fact";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static JSONObject getDictionaryJson(VocardsDataSource db, long dictId) throws JSONException {
		JSONObject res = new JSONObject();
		Cursor dictCursor = null;
		Cursor wordCursor = null;
		try {
			dictCursor = DictionaryDS.getById(db, dictId);
			wordCursor = WordDS.getOrdWordsByDictId(db, dictId);

			setdDictionaryJson(dictCursor, res);

			JSONArray words = new JSONArray();
			res.put(KEY_CARDS, words);
			wordCursor.moveToNext();
			while (!wordCursor.isAfterLast()) {
				words.put(createWordJson(wordCursor));
				wordCursor.moveToNext();
			}
		} finally {
			DBUtil.closeExistingCursor(dictCursor);
			DBUtil.closeExistingCursor(wordCursor);
		}
		return res;
	}

	public static void importDictionary(VocardsDataSource db, JSONObject dict) throws VocardsException {
		try {
			String dictName = dict.getString(KEY_DICTIONARY_NAME);
			Language natLang = Language.getById(dict.getInt(KEY_NATIVE_LANG));
			Language forLang = Language.getById(dict.getInt(KEY_FOREIGN_LANG));
			long dictId = DictionaryDS.createDictionary(db, dictName, natLang, forLang);
			
			JSONArray cards = dict.getJSONArray(KEY_CARDS);
			for (int i = 0; i < cards.length(); i++) {
				JSONObject card = cards.getJSONObject(i);
				int factor = card.getInt(KEY_CARD_FACTOR);
				String natWord = card.getJSONArray(KEY_NATIVE_WORD).getString(0);
				String forWord = card.getJSONArray(KEY_FOREIGN_WORD).getString(0);
				WordDS.createCard(db, natWord, forWord, factor, dictId);
			}
		} catch (JSONException ex) {
			throw new VocardsException("Not a dictionary JSON object.", ex);
		}
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private static void setdDictionaryJson(Cursor dictCursor, JSONObject obj) throws JSONException {
		dictCursor.moveToFirst();

		obj.put(KEY_DICTIONARY_NAME, dictCursor.getString(
				dictCursor.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_NAME)
				));
		obj.put(KEY_NATIVE_LANG, dictCursor.getString(
				dictCursor.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG)
				));
		obj.put(KEY_FOREIGN_LANG, dictCursor.getString(
				dictCursor.getColumnIndex(VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG)
				));
	}

	private static JSONObject createWordJson(Cursor c) throws JSONException {
		JSONObject res = new JSONObject();

		res.put(KEY_CARD_FACTOR, c.getInt(c.getColumnIndex(VocardsDataSource.CARD_COLUMN_FACTOR)));

		JSONArray natWords = new JSONArray();
		natWords.put(c.getString(c.getColumnIndex(WordDS.NATIVE_WORD)));

		JSONArray forWords = new JSONArray();
		forWords.put(c.getString(c.getColumnIndex(WordDS.FOREIGN_WORD)));

		res.put(KEY_NATIVE_WORD, natWords);
		res.put(KEY_FOREIGN_WORD, forWords);

		return res;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
