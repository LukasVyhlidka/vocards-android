package cz.cvut.fit.vyhliluk.vocards.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;

public class StorageUtil {
	//================= STATIC ATTRIBUTES ======================
	
	public static final String SD_CARD_ROOT_DIR = "/Android/data/";
	public static final String SD_CARD_TMP = "/tmp";

	//================= INSTANCE ATTRIBUTES ====================

	//================= STATIC METHODS =========================
	
	public static File getExternalTempDir(Context ctx) {
		String packageName = ctx.getPackageName();
		File externalPath = Environment.getExternalStorageDirectory();
		return new File(externalPath.getAbsolutePath() + SD_CARD_ROOT_DIR
				+ packageName + SD_CARD_TMP);
	}
	
	public static String readEntireFile(File f) throws IOException {
        FileReader in = new FileReader(f);
        StringBuilder contents = new StringBuilder();
        char[] buffer = new char[4096];
        int read = 0;
        do {
            contents.append(buffer, 0, read);
            read = in.read(buffer);
        } while (read >= 0);
        return contents.toString();
    }
	
	public static String readStream(InputStream is) throws IOException {
		InputStreamReader isr = new InputStreamReader(is);
        StringBuilder contents = new StringBuilder();
        char[] buffer = new char[4096];
        int read = 0;
        do {
            contents.append(buffer, 0, read);
            read = isr.read(buffer);
        } while (read >= 0);
        isr.close();
        return contents.toString();
    }

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
