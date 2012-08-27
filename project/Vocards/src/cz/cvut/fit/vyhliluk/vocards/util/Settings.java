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
	public static final String KEY_PRACTISE_DIRECTION = "practise_direction";
	public static final String KEY_CARD_FONT_SIZE = "card_font_size";
	public static final String KEY_TRANSLATION = "translation";
	public static final String KEY_LAST_BACKUP = "last_backup";
	public static final String KEY_BACKUP_AGENT_CALLED = "backupAgentCalled";

	public static final long UNDEFINED_ACTIVE_DICT_ID = -1;

	public static final int PRACTISE_DIRECTION_NATIVE_TO_FOREIGN = 0;
	public static final int PRACTISE_DIRECTION_FOREIGN_TO_NATIVE = 1;
	public static final int PRACTISE_DIRECTION_BOTH = 2;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static long getActiveDictionaryId() {
		return getLong(KEY_ACTIVE_DICT_ID, UNDEFINED_ACTIVE_DICT_ID);
	}

	public static void setActiveDictionaryId(long id) {
		putLong(KEY_ACTIVE_DICT_ID, id);
	}

	public static void removeActiveDictionary() {
		removeLong(KEY_ACTIVE_DICT_ID);
	}

	public static int getPractiseDirection() {
		return getInt(KEY_PRACTISE_DIRECTION, PRACTISE_DIRECTION_BOTH);
	}

	public static int getCardFontSize() {
		String def = "15";
		String str = getString(KEY_CARD_FONT_SIZE, def);
		if (StringUtil.isEmpty(str)) {
			remove(KEY_CARD_FONT_SIZE);
			str = def;
		}
		return Integer.parseInt(def);
	}
	
	public static boolean getTranslation() {
		return getBool(KEY_TRANSLATION, true);
	}
	
	public static void setTranslation(boolean trans) {
		putBool(KEY_TRANSLATION, trans);
	}
	
	public static long getLastBackup() {
		return getLong(KEY_LAST_BACKUP, 0);
	}
	
	public static void setLastBackup(long lastBackup) {
		putLong(KEY_LAST_BACKUP, lastBackup);
	}
	
	public static boolean getBackupAgentCalled() {
		return getBool(KEY_BACKUP_AGENT_CALLED, false);
	}
	
	public static void setBackupAgentCalled(boolean value) {
		putBool(KEY_BACKUP_AGENT_CALLED, value);
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private static long getLong(String key, long defVal) {
		return getSharedPreferences().getLong(key, defVal);
	}

	private static int getInt(String key, int defVal) {
		return getSharedPreferences().getInt(key, defVal);
	}
	
	private static String getString(String key, String defVal) {
		return getSharedPreferences().getString(key, defVal);
	}
	
	private static boolean getBool(String key, boolean defVal) {
		return getSharedPreferences().getBoolean(key, defVal);
	}

	private static void putLong(String key, Long value) {
		Editor e = getEditor();
		e.putLong(key, value);
		e.commit();
	}
	
	private static void putBool(String key, boolean value) {
		Editor e = getEditor();
		e.putBoolean(key, value);
		e.commit();
	}

	private static void removeLong(String key) {
		Editor e = getEditor();
		e.remove(key);
		e.commit();
	}
	
	private static void remove(String key) {
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
