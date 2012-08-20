package cz.cvut.fit.vyhliluk.vocards.util;

import android.app.backup.BackupManager;
import android.content.Context;
import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;

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
	
	public static void dictModif(VocardsDS db, Context ctx, long dictId) {
		DictionaryDS.setModified(db, dictId);
		
		BackupManager bckpMgr = new BackupManager(ctx);
		bckpMgr.dataChanged();
	}

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
