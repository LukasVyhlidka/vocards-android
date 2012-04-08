package cz.cvut.fit.vyhliluk.vocards.util;

import android.database.Cursor;

public class DBUtil {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================

	//================= STATIC METHODS =========================
	
	public static void closeExistingCursor(Cursor c) {
		if (c != null) {
			closeCursor(c);
		}
	}
	
	public static void closeCursor(Cursor c) {
		if (! c.isClosed()) {
			c.close();
		}
	}

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
