/*
 * Copyright (c) 2008-2011 The Negev Project
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
package negev.giyusit.staff;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import negev.giyusit.exporters.PdfExporter;
import negev.giyusit.util.DataTableDialog;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.util.row.BasicRow;
import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;

public class StaffAdminDialog extends QDialog {

	// Rulers for reports
	private static final String RULER = "ID*,FirstName,LastName,Gender,Status," + 
										"Address,City,ZipCode,HomePhone,CellPhone," + 
										"EMail,Notes";
	
	private static final String TREE_RULER = "Owner," + RULER;

    private StaffTreeModel staffModel;

	// Widgets
	private QTreeView staffTree;
	private QLineEdit name;
	private QLineEdit role;
	private QCheckBox isReal;
	
	private QGroupBox infoBox;
		
	// Buttons
	private QPushButton saveButton;
	private QPushButton addButton;
	private QPushButton removeButton;
	private QPushButton closeButton;
	
	private QPushButton ownedCandidatesButton;
	private QPushButton treeOwnedCandidatesButton;
	private QPushButton printFollowupPagesButton;
		
	public StaffAdminDialog(QWidget parent) {
		super(parent);

        staffModel = new StaffTreeModel();
		
		initUI();
		rebuildTree();
	}
		
	private void initUI() {
		setWindowTitle(tr("Staff Admin"));
			
		//
		// Widgets
		//
		staffTree = new QTreeView();
        staffTree.setModel(staffModel);
		staffTree.header().hide();
		
		// Active drag-and-drop moving of items in the tree
		/*
		staffTree.setDragEnabled(true);
		staffTree.setDropIndicatorShown(true);
		staffTree.viewport().setAcceptDrops(true);
		staffTree.setDragDropMode(QAbstractItemView.DragDropMode.InternalMove);
		*/

        staffTree.collapsed.connect(staffModel, "releaseChildren(QModelIndex)");
		staffTree.selectionModel().currentChanged.connect(
								this, "currentIndexChanged(QModelIndex)");
		
		name = new QLineEdit();
		role = new QLineEdit();
		isReal = new QCheckBox(tr("Real?"));
		
		saveButton = new QPushButton(tr("Save"));
		saveButton.setIcon(new QIcon("classpath:/icons/save.png"));
		saveButton.clicked.connect(this, "saveStaffData()");
		
		ownedCandidatesButton = new QPushButton(tr("Owned Candidates"));
		ownedCandidatesButton.clicked.connect(this, "ownedCandidates()");
		
		treeOwnedCandidatesButton = new QPushButton(tr("Tree Owned Candidates"));
		treeOwnedCandidatesButton.clicked.connect(this, "treeOwnedCandidates()");
		
		printFollowupPagesButton = new QPushButton(tr("Print Followup Pages"));
		printFollowupPagesButton.clicked.connect(this, "printFollowupPages()");
		
		addButton = new QPushButton(tr("Add"));
		addButton.setIcon(new QIcon("classpath:/icons/add.png"));
		addButton.clicked.connect(this, "addStaffMember()");
		
		removeButton = new QPushButton(tr("Remove"));
		removeButton.setIcon(new QIcon("classpath:/icons/remove.png"));
		removeButton.clicked.connect(this, "removeStaffMember()");
		
		closeButton = new QPushButton(tr("Close"));
		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
		closeButton.clicked.connect(this, "close()");
						     
		//
		// Layout
		//
		QFormLayout infoInnerLayout = new QFormLayout();
		infoInnerLayout.addRow(tr("Name: "),	name);
		infoInnerLayout.addRow(tr("Role: "),	role);
		infoInnerLayout.addRow(isReal);
		
		QHBoxLayout saveButtonLayout = new QHBoxLayout();
		saveButtonLayout.setMargin(0);
		saveButtonLayout.addStretch(1);
		saveButtonLayout.addWidget(saveButton);
		
		infoBox = new QGroupBox(tr("Details"));
			
		QVBoxLayout infoOuterLayout = new QVBoxLayout(infoBox);
		infoOuterLayout.addLayout(infoInnerLayout);
		infoOuterLayout.addLayout(saveButtonLayout);
			
		QGroupBox actionButtonsBox = new QGroupBox();
		
		QHBoxLayout actionButtonsLayout = new QHBoxLayout(actionButtonsBox);
		actionButtonsLayout.addWidget(ownedCandidatesButton);
		actionButtonsLayout.addWidget(treeOwnedCandidatesButton);
		actionButtonsLayout.addWidget(printFollowupPagesButton);
		
		QVBoxLayout leftLayout = new QVBoxLayout();
		leftLayout.addWidget(infoBox);
		leftLayout.addWidget(actionButtonsBox);
		leftLayout.addStretch(1);
		
		QHBoxLayout topLayout = new QHBoxLayout();
		topLayout.addWidget(staffTree, 1);
		topLayout.addLayout(leftLayout, 2);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(addButton);
		buttonLayout.addWidget(removeButton);
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addLayout(topLayout, 1);
		layout.addLayout(buttonLayout);		     
	}
	
	private void rebuildTree() {
		try {
			staffModel.rebuildModel();
            staffTree.expandAll();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
	}
	
	private void setUIEnabled(boolean enabled) {
		infoBox.setEnabled(enabled);
		
        removeButton.setEnabled(enabled);
        ownedCandidatesButton.setEnabled(enabled);
        treeOwnedCandidatesButton.setEnabled(enabled);
	}
	
	private void currentIndexChanged(QModelIndex index) {
		if (index == null) {
			return;
        }
		
        if (staffModel.isRoot(index)) {
        	// The root item selected
        	setUIEnabled(false);
        	
        	// Clear fields
        	name.setText("");
    		role.setText("");
    		isReal.setChecked(false);
       	}
        else {
        	setUIEnabled(true);
        	
        	StaffHelper helper = new StaffHelper();
        	
        	try {
        		int id = staffModel.indexId(index);
        		Row member = helper.fetchById(id);
        		
        		name.setText(member.getString("Name"));
        		role.setText(member.getString("Role"));
        		isReal.setChecked(member.getBoolean("RealInd"));
        	}
        	catch (Exception e) {
				MessageDialog.showException(this, e);
			}
        	finally {
        		helper.close();
        	}
        }
	}
	
	private void saveStaffData() {
		QModelIndex index = staffTree.currentIndex();
		
		if (index == null) {
			return;
        }
		
		StaffHelper helper = new StaffHelper();
    	
    	try {
    		int id = staffModel.indexId(index);
    		Row member = new BasicRow();
    		
    		member.put("Name", name.text());
    		member.put("Role", role.text());
    		member.put("RealInd", isReal.isChecked());
    		
    		helper.updateRecord(id, member);
    		
    		// Update the relevant item in the tree
    		staffModel.updateName(index, name.text());
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
	
	private void addStaffMember() {
		String name = QInputDialog.getText(this, tr("New Staff Member"), 
										tr("Enter a name for the new member"));
		
		if (Strings.isNullOrEmpty(name)) {
			return;
        }
		
		// Calculate parent id
		QModelIndex currentIndex = staffTree.currentIndex();
		Object parentId;
		
		if (currentIndex == null || staffModel.isRoot(currentIndex)) {
			parentId = null;
        }
		else {
			parentId = staffModel.indexId(currentIndex);
        }
			
		// Prepare the row
		Row row = new BasicRow();
		row.put("Name", name);
		row.put("ParentID", parentId);
		
		// Into the DB
		StaffHelper helper = new StaffHelper();
		
		try {
    		int newId = helper.insertRecord(row);
    		
    		// Refresh tree and move to the new record
    		rebuildTree();
    		staffTree.setCurrentIndex(staffModel.getIndexById(newId));
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
	
	private void removeStaffMember() {
		QModelIndex currentIndex = staffTree.currentIndex();
		    
		if (currentIndex == null || staffModel.isRoot(currentIndex)) {
			return;
        }
		
		int id = staffModel.indexId(currentIndex);
		String name = staffModel.text(staffModel.indexToValue(currentIndex));
		
		// "Are you sure?"
		QMessageBox.StandardButton ret;
		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();
		
		buttons.set(QMessageBox.StandardButton.Yes);
		buttons.set(QMessageBox.StandardButton.No);
		
		String msg = MessageFormat.format(tr("Are you sure that you wish to remove" + 
											 	" the staff member {0}?"), name);
		
		ret = QMessageBox.question(this, tr("Giyusit"), msg, buttons);
		
		if (ret == QMessageBox.StandardButton.No)
			return;
		
		// Update DB
		StaffHelper helper = new StaffHelper();
		
		try {
    		helper.deleteRecord(id);
    		
    		// Refresh tree
    		rebuildTree();
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
	
	private void ownedCandidates() {
		QModelIndex currentIndex = staffTree.currentIndex();
		    
		if (currentIndex == null || staffModel.isRoot(currentIndex)) {
			return;
        }
		
		int id = staffModel.indexId(currentIndex);
		String name = staffModel.text(staffModel.indexToValue(currentIndex));
		
		StaffHelper helper = new StaffHelper();
		
		try {
    		RowSet candidates = helper.getOwnedCandidates(id);
    		
    		// Create dialog and model
    		DataTableDialog dlg = new DataTableDialog(this);
    		
    		dlg.setWindowTitle(MessageFormat.format(tr("Candidates owned by {0}"), name));
    		dlg.resize((int) (dlg.width() * 1.1), dlg.height());
    		
    		RowSetModel model = new RowSetModel(RULER);
    		model.setRowSet(candidates);
    		
    		// Translate column headers
    		DBValuesTranslator.translateModelHeaders(model);
    		
    		// Show results
			dlg.getDataTable().setModel(model);
			dlg.exec();
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
	
	private void treeOwnedCandidates() {
        QModelIndex currentIndex = staffTree.currentIndex();

        if (currentIndex == null || staffModel.isRoot(currentIndex)) {
            return;
        }

        int id = staffModel.indexId(currentIndex);
        String name = staffModel.text(staffModel.indexToValue(currentIndex));
        StaffHelper helper = new StaffHelper();
		
		try {
    		RowSet candidates = helper.getTreeOwnedCandidates(id);
    		
    		// Create dialog and model
    		DataTableDialog dlg = new DataTableDialog(this);
    		
    		dlg.setWindowTitle(MessageFormat.format(tr("Candidates tree owned by {0}"), name));
    		dlg.resize((int) (dlg.width() * 1.1), dlg.height());
    		
    		RowSetModel model = new RowSetModel(TREE_RULER);
    		model.setRowSet(candidates);
    		
    		// Translate column headers
    		DBValuesTranslator.translateModelHeaders(model);
    		
    		// Show results
			dlg.getDataTable().setModel(model);
			dlg.exec();
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
	
	private void printFollowupPages() {
		StaffHelper helper = new StaffHelper();
		
		try {
    		RowSet realStaff = helper.getRealStaffMemebers();
    		
    		List<QAbstractItemModel> models = Lists.newArrayList();
    		List<String> titles = Lists.newArrayList();
    		
    		// Build a model for every real staff member that owns candidates
    		for (Row member : realStaff) {
    			RowSet candidates = helper.getOwnedCandidates(member.getInt("ID"));
    			if (candidates.size() == 0) {
    				continue;
                }
    			
    			RowSetModel model = new RowSetModel(RULER);
    			model.setRowSet(candidates);
    			
    			DBValuesTranslator.translateModelHeaders(model);
    			
    			// Add to lists
    			models.add(model);
    			titles.add(MessageFormat.format(tr("Candidates owned by {0}"), 
    						member.getString("Name")));
    		}
    		
    		// Export to a temporary PDF file
    		PdfExporter exporter = new PdfExporter();
			exporter.setOrientation(QPrinter.Orientation.Landscape);
			
			String fileName = File.createTempFile("giyusit_", ".pdf").getAbsolutePath();
			
			exporter.exportBatch(models, titles, fileName);
			QDesktopServices.openUrl(QUrl.fromLocalFile(fileName));
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
}
