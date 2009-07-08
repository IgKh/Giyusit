/*
 * Copyright (c) 2008-2009 The Negev Project
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of The Negev Project nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package negev.giyusit.candidates;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.MessageFormat;

import negev.giyusit.db.ConnectionProvider;
import negev.giyusit.util.CitiesCompleter;
import negev.giyusit.util.DBColumnCompleter;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.widgets.DialogField;

/**
 * A small value class representing a column in the AddCandidatesDialog
 */
class DataColumn {
	public String name;				// DB column name
	public boolean autoShrink;		// Should this column be shrinked
	public QCompleter completer;	// Completer for the column
	public QValidator validator;	// Validator for the column
	
	public DataColumn(String name, boolean autoShrink, QCompleter completer, QValidator validator) {
		this.name = name;
		this.autoShrink = autoShrink;
		this.completer = completer;
		this.validator = validator;
	}
}

public class AddCandidatesDialog extends QDialog {
	
	private static final QRegExp genderPattern = new QRegExp("[זנ]");
	
	final DataColumn[] columns = {
		new DataColumn("FirstName", false, null, null),
		new DataColumn("LastName", false, null, null),
		new DataColumn("Gender", true, null, new QRegExpValidator(genderPattern, this)),
		new DataColumn("Address", false, null, null),
		new DataColumn("City", false, new CitiesCompleter(), null),
		new DataColumn("ZipCode", true, null, new QIntValidator(this)),
		new DataColumn("HomePhone", false, null, null),
		new DataColumn("CellPhone", false, null, null),
		new DataColumn("EMail", false, null, null),
		new DataColumn("School", false, new DBColumnCompleter("Candidates", "School"), null)
	};
	
	// Widgets
	private QTableWidget tableWidget;
	private QLineEdit origin;
	
	// Buttons
	private QPushButton addButton;
	private QPushButton cancelButton;
	
	public AddCandidatesDialog(QWidget parent) {
		initUI();
		
		// Setup the table columns
		tableWidget.setColumnCount(columns.length);
		
		for (int i = 0; i < columns.length; i++) {
			QTableWidgetItem item = new QTableWidgetItem(columns[i].name);
			tableWidget.setHorizontalHeaderItem(i, item);
			
			if (columns[i].autoShrink)
				tableWidget.resizeColumnToContents(i);
		}
		
		DBValuesTranslator.translateModelHeaders(tableWidget.model());
		
		// Setup table delegate
		tableWidget.setItemDelegate(new ExTableDelegate(this));
		
		// 15 is a nice default, but ideally this should be Excel-like 
		// inifinite rows
		tableWidget.setRowCount(15);
		
		// Other completers
		origin.setCompleter(new DBColumnCompleter("Candidates", "Origin"));
	}
	
	private void initUI() {
		setWindowTitle(tr("Add Candidates"));
		
		//
		// Widgets
		//
		tableWidget = new ExTableWidget();
		
		origin = new QLineEdit();
		
		addButton = new QPushButton(tr("Add"));
		addButton.setIcon(new QIcon("classpath:/icons/add.png"));
		addButton.clicked.connect(this, "add()");
		
		cancelButton = new QPushButton(tr("Cancel"));
		cancelButton.clicked.connect(this, "reject()");
		
		//
		// Layout
		//
		QGroupBox groupData = new QGroupBox(tr("Group Data"));
		
		QHBoxLayout groupDataLayout = new QHBoxLayout(groupData);
		groupDataLayout.addWidget(new DialogField(tr("Origin: "), origin), 1);
		groupDataLayout.addStretch(3);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(addButton);
		buttonLayout.addWidget(cancelButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(groupData);
		layout.addWidget(tableWidget, 1);
		layout.addLayout(buttonLayout);
	}
	
	private void add() {
		try {
			int count = doAdd();
			String msg = tr("%n candidate(s) added succesfully", "", count);
			
			MessageDialog.showSuccess(this, msg); 
			accept();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e); 
		}
	}
	
	private int doAdd() {
		// Create a SQL template
		ArrayList<String> colsToInsert = new ArrayList<String>();
		
		for (int i = 0; i < columns.length; i++) {
			colsToInsert.add(columns[i].name);
		}
		colsToInsert.add("Origin");
		
		String sql = CandidateHelper.createInsertTemplate("Candidates", colsToInsert);
		
		// Start the database work
		Connection conn = ConnectionProvider.getConnection();
		PreparedStatement stmnt = null;
		
		try {
			stmnt = conn.prepareStatement(sql);
			
			int addedRows = 0;
			
			// For every row
			int rows = tableWidget.rowCount();
			for (int i = 0; i < rows; i++) {
				// Make sure there is something in the "FirstName" item
				QTableWidgetItem fnameItem = tableWidget.item(i, 0);
				
				if (fnameItem == null || fnameItem.text().isEmpty())
					continue;
				
				// The column from the table
				int j;
				for (j = 0; j < columns.length; j++) {
					QTableWidgetItem item = tableWidget.item(i, j);
					String value;
					
					value = (item == null) ? null : item.text();
					
					if (value == null || value.isEmpty())
						stmnt.setNull(j + 1, java.sql.Types.VARCHAR);
					else
						stmnt.setObject(j + 1, value);
				}
				
				// Group data
				stmnt.setObject(j + 1, origin.text());
				
				// Add to the batch
				stmnt.addBatch();
				addedRows++;
			}
			
			// Commit DB
			conn.setAutoCommit(false);
			stmnt.executeBatch();
			conn.setAutoCommit(true);
			
			return addedRows;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (stmnt != null) {
				try { stmnt.close(); } catch (Exception e) {}
			}
			
			try { conn.close(); } catch (Exception e) {}
		}
	}
}

class ExTableWidget extends QTableWidget {
	
	@Override
	protected void keyPressEvent(QKeyEvent e) {
		if (currentRow() == (rowCount() - 1) && currentColumn() == (columnCount() - 1)) {
			if (e.key() == Qt.Key.Key_Tab.value()) {
				setRowCount(rowCount() + 1);
			}
		}
		super.keyPressEvent(e);
	}
	
	@Override
	public QSize sizeHint() {
		return new QSize(1000, 400);
	}
}

class ExTableDelegate extends QItemDelegate {

	private AddCandidatesDialog dialog;

	public ExTableDelegate(AddCandidatesDialog parent) {
		super(parent);	
		
		this.dialog = parent;
	}
	
	@Override
	public QWidget createEditor(QWidget parent, QStyleOptionViewItem option, QModelIndex index) {
		QLineEdit editor = new QLineEdit(parent);
		
		// Set completer and validator, if neccessary
		DataColumn col = dialog.columns[index.column()];
		
		if (col.completer != null) {
			// Pop-up completion seems to break the view
			col.completer.setCompletionMode(QCompleter.CompletionMode.InlineCompletion);
			
			editor.setCompleter(col.completer);
		}
		
		if (col.validator != null)
			editor.setValidator(col.validator);
		
		return editor;
	}
	
	@Override
	public void setEditorData(QWidget editor, QModelIndex index) {
		Object obj = index.model().data(index);
		QLineEdit edit = (QLineEdit) editor;
		
		if (obj != null)
			edit.setText(obj.toString());
	}
	
	@Override
	public void setModelData(QWidget editor, QAbstractItemModel model, QModelIndex index) {
		QLineEdit edit = (QLineEdit) editor;
		
		model.setData(index, edit.text(), Qt.ItemDataRole.EditRole);
	}
	
	@Override
	public void updateEditorGeometry(QWidget editor, QStyleOptionViewItem option, QModelIndex index) {
		editor.setGeometry(option.rect());
	}
}
