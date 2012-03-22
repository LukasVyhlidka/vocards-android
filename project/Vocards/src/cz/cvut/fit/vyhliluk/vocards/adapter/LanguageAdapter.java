package cz.cvut.fit.vyhliluk.vocards.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;

public class LanguageAdapter extends ArrayAdapter<Language> {

	// ================= STATIC ATTRIBUTES ======================

	// ================= INSTANCE ATTRIBUTES ====================

	private int resourceId;
	private Context context;

	// ================= STATIC METHODS =========================

	// ================= CONSTRUCTORS ===========================

	public LanguageAdapter(Context context, int resourceId, List<Language> langs) {
		super(context, resourceId, langs);

		this.resourceId = resourceId;
		this.context = context;
	}

	

	// ================= OVERRIDEN METHODS ======================
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHandler viewHandler = null;
		
		if (convertView == null) {
			convertView = inflater.inflate(this.resourceId, null, true);
			viewHandler = new ViewHandler();
			viewHandler.langIcon = (ImageView) convertView.findViewById(R.id.languageIcon);
			viewHandler.langText = (TextView) convertView.findViewById(R.id.languageText);
			convertView.setTag(viewHandler);
		} else {
			viewHandler = (ViewHandler) convertView.getTag();
		}
		
		Language lang = this.getItem(position);
		String text = context.getResources().getString(lang.getStringId());
		viewHandler.langIcon.setImageResource(lang.getIconId());
		viewHandler.langText.setText(text);
		
		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return this.getView(position, convertView, parent);
	}

	// ================= INSTANCE METHODS =======================

	// ================= PRIVATE METHODS ========================

	// ================= GETTERS/SETTERS ========================

	// ================= INNER CLASSES ==========================

	private static class ViewHandler {
		public ImageView langIcon;
		public TextView langText;
	}

}
