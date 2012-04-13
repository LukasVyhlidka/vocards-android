package cz.cvut.fit.vyhliluk.vocards.util.component;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import cz.cvut.fit.vyhliluk.vocards.R;

public class ComponentFactory {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================

	//================= STATIC METHODS =========================
	
	public static LinearLayout createHorizLinLay(Context ctx, View... views) {
		LinearLayout res = new LinearLayout(ctx);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);
		res.setOrientation(LinearLayout.HORIZONTAL);
		res.setLayoutParams(params);
		
		for (View v : views) {
			res.addView(v);
		}
		
		return res;
	}
	
	public static EditText createEditText(Context ctx) {
		EditText edit = new EditText(ctx);
		LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		edit.setLayoutParams(params);
		return edit;
	}
	
	public static ImageView createMinusIcon(Context ctx) {
		ImageView img = new ImageView(ctx);
		
		img.setImageResource(R.drawable.word_remove);
		Resources res = ctx.getResources();
		int pxSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, res.getDisplayMetrics());
		int marginLeft = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics());
		int marginBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, res.getDisplayMetrics());
		
		LayoutParams params = new LayoutParams(pxSize, pxSize);
		params.setMargins(marginLeft, 0, 0, marginBottom);
		img.setLayoutParams(params);
		
		return img;
	}

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
