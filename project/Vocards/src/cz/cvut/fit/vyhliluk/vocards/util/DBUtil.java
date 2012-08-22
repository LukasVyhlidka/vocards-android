package cz.cvut.fit.vyhliluk.vocards.util;

import android.app.backup.BackupManager;
import android.database.Cursor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.VocardsApp;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

public class DBUtil {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static void closeExistingCursor(Cursor c) {
		if (c != null) {
			closeCursor(c);
		}
	}

	public static void closeCursor(Cursor c) {
		if (!c.isClosed()) {
			c.close();
		}
	}

	public static void dictModif(VocardsDS db, long dictId) {
		DictionaryDS.setModified(db, dictId);

		if (!Settings.getBackupAgentCalled()) {
			Log.i("DBUtil", "Calling backup agent");
			BackupManager bckpMgr = new BackupManager(VocardsApp.getInstance());
			bckpMgr.dataChanged();
			Settings.setBackupAgentCalled(true);
		}
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
