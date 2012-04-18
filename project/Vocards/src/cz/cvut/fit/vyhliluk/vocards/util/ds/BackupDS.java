package cz.cvut.fit.vyhliluk.vocards.util.ds;

import android.content.ContentValues;
import android.database.Cursor;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;

public class BackupDS {
	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static long createBackup(VocardsDataSource db, long dictId) {
		return createBackup(db, dictId, -1);
	}

	public static long createBackup(VocardsDataSource db, long dictId, long backupId) {
		ContentValues val = new ContentValues();
		if (backupId != -1) {
			val.put(VocardsDataSource.BACKUP_COLUMN_ID, backupId);
		}
		val.put(VocardsDataSource.BACKUP_COLUMN_DICTIONARY, dictId);
		val.put(VocardsDataSource.BACKUP_COLUMN_STATE, VocardsDataSource.BACKUP_STATE_BACKUPED);
		return db.insert(VocardsDataSource.BACKUP_TABLE, val);
	}

	public static int setDeleted(VocardsDataSource db, long dictId) {
		ContentValues val = new ContentValues();
		val.put(VocardsDataSource.BACKUP_COLUMN_STATE, VocardsDataSource.BACKUP_STATE_DELETED);
		val.putNull(VocardsDataSource.BACKUP_COLUMN_DICTIONARY);
		return db.update(
				VocardsDataSource.BACKUP_TABLE,
				val,
				VocardsDataSource.BACKUP_COLUMN_DICTIONARY +"=?",
				new String[] { dictId + "" });
	}
	
	public static int deleteBackup(VocardsDataSource db, long backupId) {
		return db.delete(VocardsDataSource.BACKUP_TABLE, backupId);
	}

	public static Cursor getDeleted(VocardsDataSource db) {
		return db.query(
				VocardsDataSource.BACKUP_TABLE,
				null,
				VocardsDataSource.BACKUP_COLUMN_STATE + "=?",
				new String[] { VocardsDataSource.BACKUP_STATE_DELETED + "" });
	}

	public static Cursor getByDictId(VocardsDataSource db, long dictId) {
		return db.query(
				VocardsDataSource.BACKUP_TABLE,
				null,
				VocardsDataSource.BACKUP_COLUMN_DICTIONARY + "=?",
				new String[] { dictId + "" });
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
