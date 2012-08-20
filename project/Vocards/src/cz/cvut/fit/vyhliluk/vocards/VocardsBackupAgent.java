package cz.cvut.fit.vyhliluk.vocards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.BackupDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionarySerialization;

public class VocardsBackupAgent extends BackupAgent {
	// ================= STATIC ATTRIBUTES ======================

	// private static final String KEY_DATA = "data";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		Log.i("Vocards Backup", "Backup start");

		long lastBackup = -1;
		if (oldState != null) {
			FileInputStream instream = new FileInputStream(oldState.getFileDescriptor());
			DataInputStream in = new DataInputStream(instream);

			try {
				lastBackup = in.readLong();
			} catch (IOException e) {
			}
		}

		VocardsDS db = new VocardsDS(this);
		db.open();
		try {

			this.wipeDeleted(db, data);
			this.backupModified(db, data, lastBackup);

			FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
			DataOutputStream out = new DataOutputStream(outstream);

			long backupTime = System.currentTimeMillis();
			out.writeLong(backupTime);

			Log.i("Vocards Backup", "Backup done");
		} finally {
			db.close();
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		Log.i("Vocards Backup", "Restore start");
		VocardsDS db = new VocardsDS(this);
		db.open();
		try {
			db.begin();
			while (data.readNextHeader()) {
				String key = data.getKey();
				try {
					long backupId = Long.parseLong(key);
					int dataSize = data.getDataSize();

					// Create an input stream for the BackupDataInput
					byte[] dataBuf = new byte[dataSize];
					data.readEntityData(dataBuf, 0, dataSize);
					ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
					DataInputStream in = new DataInputStream(baStream);

					// Read the player name and score from the backup data
					String json = in.readUTF();
					JSONObject dictJson = new JSONObject(json);

					long dictId = DictionarySerialization.importDictionary(db, dictJson);

					BackupDS.createBackup(db, dictId, backupId);
				} catch (NumberFormatException ex) {
					Log.e("Vocards restore", "key id = " + key);
					data.skipEntityData();
				}
			}
			db.commit();
		} catch (VocardsException ex) {
			db.rollback();
			Log.e("Vocards restore error", ex.getLocalizedMessage());
		} catch (JSONException ex) {
			db.rollback();
			Log.e("Vocards restore error", ex.getLocalizedMessage());
		} finally {
			db.close();
		}

		Log.i("Vocards Backup", "Restore done");
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private void wipeDeleted(VocardsDS db, BackupDataOutput data) throws IOException {
		db.begin();
		Cursor c = BackupDS.getDeleted(db);
		c.moveToNext();
		while (!c.isAfterLast()) {
			long backupId = c.getLong(c.getColumnIndex(VocardsDS.BACKUP_COL_ID));
			Log.i("Vocards Backup", "wiping backupId=" + backupId);
			data.writeEntityHeader(backupId + "", -1);
			BackupDS.deleteBackup(db, backupId);
			c.moveToNext();
		}
		c.close();
		db.commit();
	}

	private void backupModified(VocardsDS db, BackupDataOutput data, long lastBackup) throws IOException {
		Cursor c = DictionaryDS.getModifiedDicts(db, lastBackup);
		c.moveToNext();
		try {
			while (!c.isAfterLast()) {
				long dictId = c.getLong(c.getColumnIndex(VocardsDS.DICT_COL_ID));
				long backupId = -1;

				Cursor backupCursor = BackupDS.getByDictId(db, dictId);
				if (backupCursor.getCount() == 0) {
					backupId = BackupDS.createBackup(db, dictId);
				} else {
					backupCursor.moveToFirst();
					backupId = backupCursor.getLong(backupCursor.getColumnIndex(VocardsDS.BACKUP_COL_ID));
				}
				backupCursor.close();

				Log.i("Vocards Backup", "saving dictId=" + dictId + "; backupId=" + backupId);
				try {
					JSONObject dictJson = DictionarySerialization.getDictionaryJson(db, dictId);
					ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
					DataOutputStream outWriter = new DataOutputStream(bufStream);

					// Write structured data
					outWriter.writeUTF(dictJson.toString());

					// Send the data to the Backup Manager via the
					// BackupDataOutput
					byte[] buffer = bufStream.toByteArray();
					int len = buffer.length;
					data.writeEntityHeader(backupId + "", len);
					data.writeEntityData(buffer, len);
				} catch (JSONException ex) {
					Log.e("Vocards backup", "Error during data backup (dict id = " + dictId + "): " + ex.getLocalizedMessage());
				}

				c.moveToNext();
			}
		} finally {
			c.close();
		}
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
