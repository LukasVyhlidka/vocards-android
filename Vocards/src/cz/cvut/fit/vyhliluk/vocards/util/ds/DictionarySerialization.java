package cz.cvut.fit.vyhliluk.vocards.util.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.CardUtil;
import cz.cvut.fit.vyhliluk.vocards.util.DBUtil;

/**
 * TODO: Pri serializaci neukladam ani nenacitam hierarchii slovniku
 * 
 * @author Lucky
 * 
 */
public class DictionarySerialization {
	// ================= STATIC ATTRIBUTES ======================

	public static final String VOCARDS_MIME = "application/json";

	public static final String EXPORT_FILE_PREFIX = "vocardsExport";
	public static final String EXPORT_FILE_SUFFIX = ".xml";

	public static final String KEY_DICTIONARY_LIST = "dicts";

	public static final String KEY_DICTIONARY_BACKUP_ID = "id";
	public static final String KEY_DICTIONARY_NAME = "dictName";
	public static final String KEY_NATIVE_LANG = "natLang";
	public static final String KEY_FOREIGN_LANG = "forLang";
	public static final String KEY_CARDS = "cards";

	public static final String KEY_FOREIGN_WORD = "for";
	public static final String KEY_NATIVE_WORD = "nat";
	public static final String KEY_CARD_FACTOR = "fact";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static JSONObject getDictionariesJson(VocardsDS db, Long... dictIds) throws JSONException {
		JSONObject root = new JSONObject();
		JSONArray dictArray = new JSONArray();

		for (long id : dictIds) {
			JSONObject jsonDict = DictionarySerialization.getDictionaryJson(db, id);
			dictArray.put(jsonDict);
		}

		root.put(DictionarySerialization.KEY_DICTIONARY_LIST, dictArray);

		return root;
	}

	public static JSONObject getDictionaryJson(VocardsDS db, long dictId) throws JSONException {
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
				words.put(createWordJson(db, wordCursor.getLong(wordCursor.getColumnIndex(VocardsDS.CARD_COL_ID))));
				wordCursor.moveToNext();
			}
		} finally {
			DBUtil.closeExistingCursor(dictCursor);
			DBUtil.closeExistingCursor(wordCursor);
		}
		return res;
	}

	/**
	 * Hierarchy is returned as array in the form [ancestor, descendant, length]
	 * 
	 * @param hier
	 * @param translationMap
	 * @return
	 */
	public static JSONArray createHierarchyObj(Cursor hier, Map<Long, Long> translationMap) {
		JSONArray res = new JSONArray();

		long ancestor = hier.getLong(hier.getColumnIndex(VocardsDS.HIER_COL_ANCESTOR));
		long descendant = hier.getLong(hier.getColumnIndex(VocardsDS.HIER_COL_DESCENDANT));
		
		if (! translationMap.containsKey(ancestor) || ! translationMap.containsKey(descendant)) {
			Log.e("Hierarchy obj creation", "Translation map does not contain dict id!!!");
		}

		res.put(translationMap.get(ancestor));
		res.put(translationMap.get(descendant));
		res.put(hier.getInt(hier.getColumnIndex(VocardsDS.HIER_COL_LENGTH)));
		return res;
	}
	
	public static void insertHierarchyFromObj(VocardsDS db, JSONArray hier, Map<Long, Long> translationMap) throws JSONException {
			long ancestor = translationMap.get(hier.getLong(0));
			long descendant = translationMap.get(hier.getLong(1));
			int length = hier.getInt(2);
			
			DictionaryDS.createHierarchy(db, ancestor, descendant, length);
	}

	// public static void importDictionaries(VocardsDS db, JSONObject root)
	// throws VocardsException {
	// try {
	// JSONArray dicts =
	// root.getJSONArray(DictionarySerialization.KEY_DICTIONARY_LIST);
	// for (int i = 0; i < dicts.length(); i++) {
	// JSONObject dict = dicts.getJSONObject(i);
	// importDictionary(db, dict);
	// }
	// } catch (JSONException ex) {
	// throw new VocardsException("Not a dictionary JSON object.", ex);
	// }
	// }

	public static long importDictionary(VocardsDS db, JSONObject dict) throws VocardsException {
		try {
			String dictName = dict.getString(KEY_DICTIONARY_NAME);
			Language natLang = Language.getById(dict.getInt(KEY_NATIVE_LANG));
			Language forLang = Language.getById(dict.getInt(KEY_FOREIGN_LANG));

			long dictId = DictionaryDS.createDictionary(db, dictName, natLang, forLang, null);

			JSONArray cards = dict.getJSONArray(KEY_CARDS);
			for (int i = 0; i < cards.length(); i++) {
				JSONObject card = cards.getJSONObject(i);
				int factor = card.getInt(KEY_CARD_FACTOR);

				List<String> natWords = new ArrayList<String>();
				JSONArray natArray = card.getJSONArray(KEY_NATIVE_WORD);
				for (int j = 0; j < natArray.length(); j++) {
					natWords.add(natArray.getString(j));
				}

				List<String> forWords = new ArrayList<String>();
				JSONArray forArray = card.getJSONArray(KEY_FOREIGN_WORD);
				for (int j = 0; j < forArray.length(); j++) {
					forWords.add(forArray.getString(j));
				}

				WordDS.createCard(db, natWords, forWords, factor, dictId);
			}
			return dictId;
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
				dictCursor.getColumnIndex(VocardsDS.DICT_COL_NAME)
				));
		obj.put(KEY_NATIVE_LANG, dictCursor.getString(
				dictCursor.getColumnIndex(VocardsDS.DICT_COL_NATIVE_LANG)
				));
		obj.put(KEY_FOREIGN_LANG, dictCursor.getString(
				dictCursor.getColumnIndex(VocardsDS.DICT_COL_FOREIGN_LANG)
				));
	}

	private static JSONObject createWordJson(VocardsDS db, long cardId) throws JSONException {
		JSONObject res = new JSONObject();

		Cursor card = WordDS.getCardById(db, cardId);

		card.moveToFirst();
		res.put(KEY_CARD_FACTOR, card.getInt(card.getColumnIndex(VocardsDS.CARD_COL_FACTOR)));

		JSONArray natWords = new JSONArray();
		List<String> natWList = CardUtil.explodeWords(card.getString(card.getColumnIndex(VocardsDS.CARD_COL_NATIVE)));
		for (String nat : natWList) {
			natWords.put(nat);
		}

		JSONArray forWords = new JSONArray();
		List<String> forWList = CardUtil.explodeWords(card.getString(card.getColumnIndex(VocardsDS.CARD_COL_FOREIGN)));
		for (String forw : forWList) {
			forWords.put(forw);
		}

		res.put(KEY_NATIVE_WORD, natWords);
		res.put(KEY_FOREIGN_WORD, forWords);

		card.close();

		return res;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
