package cz.cvut.fit.vyhliluk.vocards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import cz.cvut.fit.vyhliluk.vocards.core.VocardsException;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionaryDS;
import cz.cvut.fit.vyhliluk.vocards.util.ds.DictionarySerialization;

public class VocardsBackupAgent extends BackupAgent {
	// ================= STATIC ATTRIBUTES ======================

	private static final String KEY_DATA = "data";

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		Log.i("Vocards Backup", "Backup start");
		VocardsDataSource db = new VocardsDataSource(this);
		db.open();

		List<Long> allDictIds = DictionaryDS.getDictIds(db);

		try {
			JSONObject bckp = DictionarySerialization.getDictionariesJson(db, allDictIds.toArray(new Long[] {}));
			ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
			DataOutputStream outWriter = new DataOutputStream(bufStream);

			// Write structured data
			outWriter.writeUTF(bckp.toString());

			// Send the data to the Backup Manager via the BackupDataOutput
			byte[] buffer = bufStream.toByteArray();
			int len = buffer.length;
			data.writeEntityHeader(KEY_DATA, len);
			data.writeEntityData(buffer, len);
		} catch (JSONException ex) {
			Log.e("Vocards backup", "Error during data backup: " + ex.getLocalizedMessage());
		} finally {
			db.close();
		}
		
		FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
		DataOutputStream out = new DataOutputStream(outstream);

		long backupTime = System.currentTimeMillis();
		out.writeLong(backupTime);
		
		Log.i("Vocards Backup", "Backup done");
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		VocardsDataSource db = new VocardsDataSource(this);
		db.open();
		try {
			db.begin();
			while (data.readNextHeader()) {
				String key = data.getKey();
				int dataSize = data.getDataSize();

				// Create an input stream for the BackupDataInput
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
				DataInputStream in = new DataInputStream(baStream);

				// Read the player name and score from the backup data
				String json = in.readUTF();
				JSONObject root = new JSONObject(json);

				DictionarySerialization.importDictionaries(db, root);
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

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
