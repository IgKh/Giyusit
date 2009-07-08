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

import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.util.RowSet;
import negev.giyusit.util.Row;
import negev.giyusit.widgets.DataGrid;

/**
 * A configuration sheet for defining possible values for candidate statuses.
 * Meant to be embedded in TableAdminDialog
 */
public class CandidateStatusValuesAdmin extends QWidget {

	// Widgets
	private DataGrid dataGrid;
	
	// Buttons
	private QPushButton addButton;
	private QPushButton removeButton;
	
	//
	private RowSetModel model;

	public CandidateStatusValuesAdmin(QWidget parent) {
		super(parent);
		
		initUI();
		
		model = new RowSetModel(new String[] {"ID","Name","Active"});
		
		DBValuesTranslator.translateModelHeaders(model);
		dataGrid.setModel(model);
		
		loadData();
	}
	
	
	private void initUI() {
		//
		// Widgets
		//
		dataGrid = new DataGrid();
		
		addButton = new QPushButton(tr("Add..."));
		addButton.clicked.connect(this, "doAdd()");
		
		removeButton = new QPushButton(tr("Remove"));
		
		//
		// Layout
		//
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(addButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(dataGrid);
		layout.addLayout(buttonLayout);
	}
	
	private void loadData() {
		CSVHelper helper = new CSVHelper();
		
		try {
			model.setData(helper.getValidStatusValues());
		}
		catch (Exception e) {
			MessageDialog.showException(window(), e);
		}
		finally {
			helper.close();
		}
	}
	
	private void doAdd() {
		ItemDialog dlg = new ItemDialog(this, null);
		
		if (dlg.exec() == 1) {
			CSVHelper helper = new CSVHelper();
			Row row = dlg.getData();
		
			try {
				helper.insertRecord(row);
				
				loadData();
			}
			catch (Exception e) {
				MessageDialog.showException(window(), e);
			}
			finally {
				helper.close();
			}
		}
	}
	
	private void doRemove() {
		
	}
}

class ItemDialog extends QDialog {
	
	private QLabel id;
	private QLineEdit name;
	private QCheckBox isActive;
	
	public ItemDialog(QWidget parent, Row dataRow) {
		super(parent);
		
		initUI();
		
		if (dataRow != null) {
			loadData(dataRow);
			setWindowTitle(name.text());
		}
		else {
			id.setText(tr("<New>"));
			
			setWindowTitle(tr("New Status"));
		}
	}
	
	private void initUI() {
		id = new QLabel();
		
		name = new QLineEdit();
		
		isActive = new QCheckBox();
		isActive.setChecked(true);
		
		QPushButton okButton = new QPushButton(tr("OK"));
		okButton.clicked.connect(this, "doOk()");
		
		QPushButton cancelButton = new QPushButton(tr("Cancel"));
		cancelButton.clicked.connect(this, "reject()");
		
		// Layout
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		
		QFormLayout layout = new QFormLayout(this);
		layout.addRow(tr("ID: "), id);
		layout.addRow(tr("Name: "), name);
		layout.addRow(tr("Active? "), isActive);
		layout.addRow(buttonLayout);
	}
	
	private void doOk() {
		if (name.text().isEmpty()) {
			// TODO: alert
			return;
		}
		accept();
	}
	
	private void loadData(Row dataRow) {
		id.setText(dataRow.getString("ID"));
		name.setText(dataRow.getString("Name"));
		isActive.setChecked(dataRow.getBoolean("ActiveInd"));
	}
	
	public Row getData() {
		Row dataRow = new Row();
		
		dataRow.put("Name", name.text());
		dataRow.put("ActiveInd", isActive.isChecked());
		
		return dataRow;
	}
}

class CSVHelper extends GenericHelper {
	
	public CSVHelper() {
		super("CandidateStatusValues");
	}
	
	public RowSet getValidStatusValues() {
		String sql = "select CSV.ID, CSV.Name, BV.Value as Active " + 
						"from CandidateStatusValues CSV " + 
						"left outer join BooleanValues BV on CSV.ActiveInd = BV.Key " + 
						"where CSV.ID <> (select Value from FileParams where Key = 'DefaultCandidateStatus') " + 
						"and CSV.EndDate isnull";
		
		return getQueryWrapper().queryForRowSet(sql);
	}
}
