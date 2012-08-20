package cz.cvut.fit.vyhliluk.vocards.util.ds;

import android.content.ContentValues;
import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;

public class BackupDS {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static long createBackup(VocardsDS db, long dictId) {
		return createBackup(db, dictId, -1);
	}

	public static long createBackup(VocardsDS db, long dictId, long backupId) {
		ContentValues val = new ContentValues();
		if (backupId != -1) {
			val.put(VocardsDS.BACKUP_COL_ID, backupId);
		}
		val.put(VocardsDS.BACKUP_COL_DICTIONARY, dictId);
		val.put(VocardsDS.BACKUP_COL_STATE, VocardsDS.BACKUP_STATE_BACKUPED);
		return db.insert(VocardsDS.BACKUP_TABLE, val);
	}

	public static int setDeleted(VocardsDS db, long dictId) {
		ContentValues val = new ContentValues();
		val.put(VocardsDS.BACKUP_COL_STATE, VocardsDS.BACKUP_STATE_DELETED);
		val.putNull(VocardsDS.BACKUP_COL_DICTIONARY);
		return db.update(
				VocardsDS.BACKUP_TABLE,
				val,
				VocardsDS.BACKUP_COL_DICTIONARY +"=?",
				new String[] { dictId + "" });
	}
	
	public static int deleteBackup(VocardsDS db, long backupId) {
		return db.delete(VocardsDS.BACKUP_TABLE, backupId);
	}

	public static Cursor getDeleted(VocardsDS db) {
		return db.query(
				VocardsDS.BACKUP_TABLE,
				null,
				VocardsDS.BACKUP_COL_STATE + "=?",
				new String[] { VocardsDS.BACKUP_STATE_DELETED + "" });
	}

	public static Cursor getByDictId(VocardsDS db, long dictId) {
		return db.query(
				VocardsDS.BACKUP_TABLE,
				null,
				VocardsDS.BACKUP_COL_DICTIONARY + "=?",
				new String[] { dictId + "" });
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
