package cz.cvut.fit.vyhliluk.vocards.enums;

import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.util.Const;

/**
 * Enum representing all languages, that user can choose for the dictionary
 * 
 * @author Lucky
 * 
 */
public enum Language {
	
	NONE(Const.LANGUAGE_NONE, R.string.lang_none, 0),
	CZECH(Const.LANGUAGE_CZ, R.string.lang_cz, 0),
	ENGLISH(Const.LANGUAGE_EN, R.string.lang_en, 0),
	FRENCH(Const.LANGUAGE_FR, R.string.lang_fr, 0);
	
	private final int id;
	private final int stringId;
	private final int iconId;
	
	private Language(int id, int stringId, int iconId) {
		this.id = id;
		this.stringId = stringId;
		this.iconId = iconId;
	}

	public int getId() {
		return id;
	}

	public int getStringId() {
		return stringId;
	}

	public int getIconId() {
		return iconId;
	}
	

}
