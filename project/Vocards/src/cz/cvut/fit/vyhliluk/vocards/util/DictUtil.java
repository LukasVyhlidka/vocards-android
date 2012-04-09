package cz.cvut.fit.vyhliluk.vocards.util;

import cz.cvut.fit.vyhliluk.vocards.R;
import cz.cvut.fit.vyhliluk.vocards.enums.Language;
import cz.cvut.fit.vyhliluk.vocards.persistence.VocardsDataSource;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class DictUtil {
	//================= STATIC ATTRIBUTES ======================

	//================= INSTANCE ATTRIBUTES ====================

	//================= STATIC METHODS =========================
	
	public static SimpleCursorAdapter createDictListAdapter(Context ctx) {
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(
				ctx,
				R.layout.inf_dictionary_item,
				null,
				new String[] {
						VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG,
						VocardsDataSource.DICTIONARY_COLUMN_NAME
				},
				new int[] {
						R.id.nativeLangIcon,
						R.id.foreignLangIcon,
						R.id.languageText
				});

		ViewBinder listViewBinder = new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				String colName = cursor.getColumnName(columnIndex);
				if (VocardsDataSource.DICTIONARY_COLUMN_NATIVE_LANG.equals(colName)
						|| VocardsDataSource.DICTIONARY_COLUMN_FOREIGN_LANG.equals(colName)) {
					ImageView img = (ImageView) view;
					Language lang = Language.getById(cursor.getInt(columnIndex));
					img.setImageResource(lang.getIconId());
					return true;
				} else {
					return false;
				}
			}
		};
		
		listAdapter.setViewBinder(listViewBinder);
		return listAdapter;
	}

	//================= CONSTRUCTORS ===========================

	//================= OVERRIDEN METHODS ======================

	//================= INSTANCE METHODS =======================

	//================= PRIVATE METHODS ========================

	//================= GETTERS/SETTERS ========================

	//================= INNER CLASSES ==========================

}
