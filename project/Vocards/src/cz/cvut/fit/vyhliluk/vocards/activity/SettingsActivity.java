package cz.cvut.fit.vyhliluk.vocards.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.util.Settings;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private EditTextPreference fontSizePref = null;
	private ListPreference practDirPref = null;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(Settings.PREF_NAME);
		prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

		addPreferencesFromResource(R.xml.preferences);

		this.init();
		
		prefMgr.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {

		if (Settings.KEY_CARD_FONT_SIZE.equals(key)) {
			this.fontSizePref.setSummary(Settings.getCardFontSize()+"");
		} else if (Settings.KEY_PRACTISE_DIRECTION.equals(key)) {
			this.practDirPref.setSummary(this.getPractiseDirectionName());
		}

	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void init() {
		this.fontSizePref = (EditTextPreference) findPreference(Settings.KEY_CARD_FONT_SIZE);
		this.practDirPref = (ListPreference) findPreference(Settings.KEY_PRACTISE_DIRECTION);

		String dirName = this.getResources().getStringArray(R.array.practise_direction_array_names)[Settings.getPractiseDirection()];

		this.fontSizePref.setSummary(Settings.getCardFontSize() + "");
		this.practDirPref.setSummary(dirName);
	}

	private String getPractiseDirectionName() {
		return this.getResources().getStringArray(R.array.practise_direction_array_names)[Settings.getPractiseDirection()];
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
