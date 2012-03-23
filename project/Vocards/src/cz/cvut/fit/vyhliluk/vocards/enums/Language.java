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
	
	NONE(Const.LANGUAGE_NONE, R.string.lang_none, R.drawable.lang_cz),
	CZECH(Const.LANGUAGE_CZ, R.string.lang_cz, R.drawable.lang_cz),
	SLOVAK(Const.LANGUAGE_SK, R.string.lang_sk, R.drawable.lang_sk),
	ENGLISH(Const.LANGUAGE_EN, R.string.lang_en, R.drawable.lang_en),
	FRENCH(Const.LANGUAGE_FR, R.string.lang_fr, R.drawable.lang_fr),
	GERMAN(Const.LANGUAGE_DE, R.string.lang_de, R.drawable.lang_de);
	
	public static Language getById(int id) {
		for (Language l : Language.values()) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}
	
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
