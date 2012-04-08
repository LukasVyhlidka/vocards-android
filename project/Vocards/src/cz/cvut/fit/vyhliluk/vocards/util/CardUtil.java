package cz.cvut.fit.vyhliluk.vocards.util;

public class CardUtil {
	// ================= STATIC ATTRIBUTES ======================

	public static final int MAX_FACTOR = 10;
	public static final int MIN_FACTOR = 0;

	// ================= INSTANCE ATTRIBUTES ====================

	// ================= STATIC METHODS =========================

	public static int getNewFactor(boolean know, int factor) {
		if (know) {
			return newKnowFactor(factor);
		} else {
			return newDontKnowFactor(factor);
		}
	}
	
	public static String cardFactorPercent(int factor) {
		return (factor * 10) + " %";
	}
	
	public static String dictFactorPercent(double factor) {
		return String.format("%.2f %%", factor * 10);
	}

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	private static int newKnowFactor(int factor) {
		int res = factor;
		if (factor <= 2) {
			res += 3;
		} else if (factor <= 5) {
			res += 2;
		} else {
			res++;
		}

		if (res > MAX_FACTOR) {
			res = MAX_FACTOR;
		}

		return res;
	}

	private static int newDontKnowFactor(int factor) {
		int res = factor;
		if (factor >= 9) {
			res -= 4;
		} else if (factor >= 6) {
			res -= 3;
		} else if (factor >= 3) {
			res -= 2;
		} else {
			res--;
		}

		if (res < MIN_FACTOR) {
			res = MIN_FACTOR;
		}

		return res;
	}

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

}
