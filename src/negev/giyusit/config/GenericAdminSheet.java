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
package negev.giyusit.config;

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.List;

import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.GenericItemDialog;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.widgets.DataGrid;

public class GenericAdminSheet extends QWidget {

	// Widgets
	private DataGrid dataGrid;
	
	// Buttons
	private QPushButton addButton;
	private QPushButton removeButton;

	// Properties
	private GenericAdminHelper helper;
	private RowSetModel model;

	public GenericAdminSheet(QWidget parent, GenericAdminHelper helper) {
		super(parent);
		
		this.helper = helper;
		
		// Setup model
		model = new RowSetModel(helper.getRuler());
		DBValuesTranslator.translateModelHeaders(model);
		
		initUI();
		loadData();
		
		if (!helper.addRemoveAllowed()) {
			addButton.setVisible(false);
			removeButton.setVisible(false);
		}
	}
	
	private void initUI() {
		//
		// Widgets
		//
		dataGrid = new DataGrid();
		dataGrid.setModel(model);
		dataGrid.activated.connect(this, "update(QModelIndex)");
		
		dataGrid.selectionModel().selectionChanged.connect(this, "selectionChanged()");
		
		addButton = new QPushButton(tr("Add..."));
		addButton.setIcon(new QIcon("classpath:/icons/add.png"));
		addButton.clicked.connect(this, "add()");
		
		removeButton = new QPushButton(tr("Remove"));
		removeButton.setEnabled(false);
		removeButton.setIcon(new QIcon("classpath:/icons/remove.png"));
		removeButton.clicked.connect(this, "remove()");
		
		//
		// Layout
		//
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(addButton);
		buttonLayout.addWidget(removeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(dataGrid);
		layout.addLayout(buttonLayout);
	}
	
	private void loadData() {
		try {
			model.setRowSet(helper.getValues());
			
			dataGrid.shrinkColumns();
		}
		catch (Exception e) {
			MessageDialog.showException(window(), e);
		}
	}
	
	private void selectionChanged() {
		removeButton.setEnabled(dataGrid.selectionModel().hasSelection());
	}
	
	private void add() {
		GenericItemDialog dlg = helper.createItemDialog(window());
		
		dlg.setWindowTitle(tr("New Item"));
		
		if (dlg.exec() == QDialog.DialogCode.Accepted.value()) {
			try {
				helper.insertRecord(dlg.toRow());
				
				loadData();
			}
			catch (Exception e) {
				MessageDialog.showException(window(), e);
			}
		}
	}
	
	private void remove() {
		// Get the selected item
		List<QModelIndex> selection = dataGrid.selectionModel().selectedRows();
		
		if (selection.isEmpty() || selection.get(0) == null)
			return;
		
		QModelIndex index = selection.get(0);
		int id = QVariant.toInt(index.data(RowSetModel.ID_ROLE));
		
		// Remove this item
		try {
			helper.deleteItem(id);
			dataGrid.selectionModel().clearSelection();
			
			loadData();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
	}
	
	private void update(QModelIndex index) {
		if (index == null)
			return;
		
		// Extract record ID
		int id = QVariant.toInt(index.data(RowSetModel.ID_ROLE));
		
		// Show dialog
		GenericItemDialog dlg = helper.createItemDialog(window());
		dlg.setWindowTitle(tr("Edit Item"));
		
		try {
			dlg.fromRow(helper.fetchById(id));
			
			if (dlg.exec() == QDialog.DialogCode.Rejected.value())
				return;
			
			// Update
			helper.updateRecord(id, dlg.toRow());
			
			loadData();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
	}
}