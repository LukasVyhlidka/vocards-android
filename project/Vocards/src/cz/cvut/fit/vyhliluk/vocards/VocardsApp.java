package cz.cvut.fit.vyhliluk.vocards;

import android.app.Application;

public class VocardsApp extends Application {
	// ================= STATIC ATTRIBUTES ======================

	private static VocardsApp instance = null;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	public static VocardsApp getInstance() {
		return instance;
	}
	
	public VocardsApp() {
		super();
	}

	// ================= OVERRIDEN METHODS ======================
	
	@Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;
    }

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
