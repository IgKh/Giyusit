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
	
	private final static String SNAPSHOT_PATH = QDir.homePath() + "/.giyusit_snapshot";
	private final static String NULL_STRING = new String(new char[] {'\0'});
	
	private final QRegExp genderPattern = new QRegExp(tr("[MF]", "Gender Regexp"));
	
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
	private QLabel statusLabel;
	
	// Buttons
	private QPushButton addButton;
	private QPushButton cancelButton;
	
	// Properties
	private QTimer persistTimer;
	
	public AddCandidatesDialog(QWidget parent) {
		super(parent);
		
		initUI();
		initTable();
		restoreWindowState();
		
		// Setup table delegate
		tableWidget.setItemDelegate(new ExTableDelegate(this));
		
		// 15 is a nice default, but ideally this should be Excel-like 
		// inifinite rows
		tableWidget.setRowCount(15);
		
		// Other completers
		origin.setCompleter(new DBColumnCompleter("Candidates", "Origin"));
		
		// Create the persist timer. By default, persist every minute
		persistTimer = new QTimer(this);
		persistTimer.setInterval(60 * 1000);
		persistTimer.timeout.connect(this, "persistDataSlot()");
		persistTimer.start();
		
		// Try to restore
		tryRestore();
	}
	
	protected void closeEvent(QCloseEvent e) {
		saveWindowState();	
		
		super.closeEvent(e);
	}
	
	private void initUI() {
		setWindowTitle(tr("Add Candidates"));
		
		//
		// Widgets
		//
		tableWidget = new ExTableWidget();
		
		origin = new QLineEdit();
		
		statusLabel = new QLabel();
		
		QPushButton clearButton = new QPushButton();
		clearButton.setToolTip(tr("Clear data"));
		clearButton.setIcon(new QIcon("classpath:/icons/clear.png"));
		clearButton.clicked.connect(this, "initTable()");
		
		addButton = new QPushButton(tr("Add"));
		addButton.setIcon(new QIcon("classpath:/icons/add.png"));
		addButton.clicked.connect(this, "add()");
		
		cancelButton = new QPushButton(tr("Cancel"));
		cancelButton.clicked.connect(this, "reject()");
		
		this.rejected.connect(this, "close()");
		this.accepted.connect(this, "close()");
		
		//
		// Layout
		//
		QGroupBox groupData = new QGroupBox(tr("Group Data"));
		
		QHBoxLayout groupDataLayout = new QHBoxLayout(groupData);
		groupDataLayout.addWidget(new DialogField(tr("Origin: "), origin), 1);
		groupDataLayout.addStretch(3);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(clearButton);
		buttonLayout.addWidget(statusLabel);
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(addButton);
		buttonLayout.addWidget(cancelButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(groupData);
		layout.addWidget(tableWidget, 1);
		layout.addLayout(buttonLayout);
	}
	
	private void initTable() {
		tableWidget.clear();
		
		// Setup the table columns
		tableWidget.setColumnCount(columns.length);
		
		for (int i = 0; i < columns.length; i++) {
			QTableWidgetItem item = new QTableWidgetItem(columns[i].name);
			tableWidget.setHorizontalHeaderItem(i, item);
			
			if (columns[i].autoShrink)
				tableWidget.resizeColumnToContents(i);
		}
		
		DBValuesTranslator.translateModelHeaders(tableWidget.model());
		
		// Clear group data
		origin.setText("");
	}
	
	private void saveWindowState() {
		QSettings settings = new QSettings();
		
		settings.setValue("addCandidatesDialog/geometry", saveGeometry());
	}
	
	private void restoreWindowState() {
		QSettings settings = new QSettings();
		
		restoreGeometry((QByteArray) settings.value("addCandidatesDialog/geometry"));
	}
	
	/**
	 * Writes a binary snapshot of the dialog's current contents into
	 * a file, so if the dialog will be abruptly terminated the user's data
	 * can be restored.
	 */
	private void persistData() {
		// Open a file for writing, overriding any existing one
		QFile file = new QFile(SNAPSHOT_PATH);
		
		QIODevice.OpenMode mode = new QIODevice.OpenMode();
		mode.set(QIODevice.OpenModeFlag.WriteOnly);
		mode.set(QIODevice.OpenModeFlag.Truncate);
		
		if (!file.open(mode))
			return;
				
		QDataStream stream = new QDataStream(file);
		stream.setVersion(QDataStream.Version.Qt_4_0.value());
		
		// First, the table. Write the number of rows and columns
		QAbstractItemModel model = tableWidget.model();
		
		stream.writeInt(model.rowCount());
		stream.writeInt(model.columnCount());
		
		// Now write each row. Values are persisted as strings
		for (int i = 0; i < model.rowCount(); i++) {
			for (int j = 0; j < model.columnCount(); j++) {
				Object data = model.data(i, j);
				
				// If there is no data in a table cell, put a placeholder
				// string consisting of the null char
				if (data != null)
					stream.writeString(data.toString());
				else
					stream.writeString(NULL_STRING);
			}
		}
		
		// Write group data
		stream.writeString(origin.text());
		
		// Close file
		file.close();
	}
	
	/**
	 * Restores the dialog's contents from a previously saved binary snapshot
	 * file.
	 */
	private void restoreData() {
		// Open a file for reading
		QFile file = new QFile(SNAPSHOT_PATH);
		
		QIODevice.OpenMode mode = new QIODevice.OpenMode();
		mode.set(QIODevice.OpenModeFlag.ReadOnly);
		
		if (!file.open(mode))
			return;
				
		QDataStream stream = new QDataStream(file);
		stream.setVersion(QDataStream.Version.Qt_4_0.value());
		
		// Read the number of rows and columns
		int rows = stream.readInt();
		int cols = stream.readInt();
		
		tableWidget.setRowCount(rows);
		tableWidget.setColumnCount(cols);
		
		// Now read the table contents
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				String data = stream.readString();
				
				if (!data.equals(NULL_STRING)) {
					QTableWidgetItem item = new QTableWidgetItem(data);
					
					tableWidget.setItem(i, j, item);
				}
			}
		}
		
		// Read group data
		origin.setText(stream.readString());
		
		// Close file
		file.close();
	}
	
	/**
	 * Checks if there is a snapshot file, and tries to restore from it.
	 */
	private void tryRestore() {
		if (QFile.exists(SNAPSHOT_PATH))
			restoreData();
	}
	
	/**
	 * The slot that is called by the timer. Persists the dialog's data and
	 * updates the informative label
	 */
	private void persistDataSlot() {
		persistData();
		
		statusLabel.setText(MessageFormat.format(tr("Data autosaved at {0}"), 
			QTime.currentTime().toString()));
	}
	
	private void add() {
		try {
			// Stop the timer
			persistTimer.stop();
			
			int count = doAdd();
			
			// Show success message
			String msg = tr("%n candidate(s) added succesfully", "", count);
			MessageDialog.showSuccess(this, msg); 
			
			// Delete snapshot file and close the dialog
			if (QFile.exists(SNAPSHOT_PATH))
				QFile.remove(SNAPSHOT_PATH);
			
			accept();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e); 
			
			// Restart the timer
			persistTimer.start();
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
