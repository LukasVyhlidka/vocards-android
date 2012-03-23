package cz.cvut.fit.vyhliluk.vocards.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import cz.cvut.fit.vyhliluk.vocards.VocardsApp;

/**
 * This class represents persistent storage for user data (e.g. selected
 * dictionary).
 * 
 * @author Lucky
 * 
 */
public class Settings {
	// ================= STATIC ATTRIBUTES ======================
	
	public static final String PREF_NAME = "vocards.settings";
	
	public static final String KEY_ACTIVE_DICT_ID = "active_dict_id";
	public static final long UNDEFINED_ACTIVE_DICT_ID = -1;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================
	
	public static long getActiveDictionaryId() {
		return getLong(KEY_ACTIVE_DICT_ID);
	}
	
	public static void setActiveDictionaryId(long id) {
		putLong(KEY_ACTIVE_DICT_ID, id);
	}
	
	public static void removeActiveDictionary() {
		removeLong(KEY_ACTIVE_DICT_ID);
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================
	
	private static long getLong(String key) {
		return getSharedPreferences().getLong(key, UNDEFINED_ACTIVE_DICT_ID);
	}
	
	private static void putLong(String key, Long value) {
		Editor e = getEditor();
		e.putLong(key, value);
		e.commit();
	}
	
	private static void removeLong(String key) {
		Editor e = getEditor();
		e.remove(key);
		e.commit();
	}
	
	private static Editor getEditor() {
		return getSharedPreferences().edit();
	}
	
	private static SharedPreferences getSharedPreferences() {
		return VocardsApp.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
