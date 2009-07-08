/**
 * Copyright (c) 2008-2009 The Negev Project
 */
package negev.giyusit;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.sql.Connection;

import negev.giyusit.candidates.CandidateDataView;
import negev.giyusit.events.EventDataView;
import negev.giyusit.db.ConnectionProvider;
import negev.giyusit.db.GenericDataView;
import negev.giyusit.db.QueryWrapper;
import negev.giyusit.util.Row;
import negev.giyusit.util.RowSet;

public class DataViewList extends QToolBox {

	private static final int ID_ROLE = Qt.ItemDataRole.UserRole;

	private ArrayList<DataView> dataViews;
	
	public Signal1<DataView> dataViewSelected = new Signal1<DataView>();

	public DataViewList() {
		this(null);
	}	
	
	public DataViewList(QWidget parent) {
		super(parent);
		
		dataViews = new ArrayList<DataView>();
	}
	
	public String saveState() {
		QListWidget currentList = (QListWidget) currentWidget();
		
		return currentIndex() + ";" + currentList.currentRow();
	}
	
	public void restoreState(String state) {
		if (state == null)
			return;
		
		boolean stateRestored = false;
		
		// Run a regexp here to make sure the state string is valid
		QRegExp regexp = new QRegExp("^\\d+;\\d+$");
		
		if (!regexp.exactMatch(state))
			return;
		
		// Parse state string
		String[] parts = state.split(";");
		
		// Select QToolBox index
		int index = Integer.parseInt(parts[0]);
		
		if (index < 0 || index > count())
			return;
		
		setCurrentIndex(index);
		
		// Select QListBox item
		QListWidget currentList = (QListWidget) currentWidget();
		int row = Integer.parseInt(parts[1]);
		
		//if (row < 0 || row > currentList.rowCount())
		//	return;
		
		currentList.setCurrentRow(row);
	}
	
	public void clear() {
		dataViews.clear();
		
		while (count() > 0)
			widget(0).dispose();
	}
	
	public void rebuildList() {
		clear();
		
		// Open a connection to the database and a query wrapper
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
						
			// Get list of categories	
			String sql = "select ID, Name from DataViewCategories";
			RowSet categories = wrapper.queryForRowSet(sql);
			
			for (Row category : categories) {
				addItem(buildCategoryList(category, wrapper), 
										category.getString("Name"));
			}
		}
		finally {
			try { conn.close(); } catch (Exception e) {}
		}
	}
	
	private QListWidget buildCategoryList(Row categoryRow, QueryWrapper wrapper) {
		QListWidget listWidget = new QListWidget();
		
		listWidget.setIconSize(new QSize(42, 42));
		listWidget.currentItemChanged.connect(this, "itemSelected(QListWidgetItem)");
		
		// Get data views in category
		String sql = "select Title, Query, Ruler from DataViews where CategoryID = ?";
		String categoryId = categoryRow.getString("ID");
		
		RowSet views = wrapper.queryForRowSet(sql, categoryId);
		
		for (Row view : views) {
			String title = view.getString("Title");
			String query = view.getString("Query");
			String ruler = view.getString("Ruler");
			
			// Create DataView object
			DataView dataView;
			
			if (categoryId.equals("1"))
				dataView = new CandidateDataView(title, query, ruler);
			else if (categoryId.equals("2"))
				dataView = new EventDataView(title, query, ruler);
			else
				dataView = new GenericDataView(title, query, ruler);
			
			dataViews.add(dataView);	
			
			// Create list widget entry
			QListWidgetItem item = new QListWidgetItem(listWidget);
			item.setText(dataView.getName());
			item.setIcon(dataView.getIcon());
			item.setData(ID_ROLE, dataViews.size() - 1);
		}
		
		//
		return listWidget;
	}
	
	@SuppressWarnings("unused")
	private void itemSelected(QListWidgetItem item) {
		if (item == null)
			return;
		
		int id = Integer.parseInt(item.data(ID_ROLE).toString());
		
		dataViewSelected.emit(dataViews.get(id));
	}
}
