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
package negev.giyusit.staff;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.text.MessageFormat;

import negev.giyusit.util.DataTableDialog;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.util.RowSet;
import negev.giyusit.util.Row;

public class StaffAdminDialog extends QDialog {

	private static final int ID_ROLE = Qt.ItemDataRole.UserRole;
	private static final int ROOT_TYPE = QTreeWidgetItem.ItemType.UserType.value();

	// Rulers for reports
	private static final String RULER = "ID,FirstName,LastName,Gender,Status," + 
										"Address,City,ZipCode,HomePhone,CellPhone,EMail";
	
	private static final String TREE_RULER = RULER + ",Owner";

	// Widgets
	private QTreeWidget staffTree;
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
		
	public StaffAdminDialog(QWidget parent) {
		super(parent);
		
		initUI();
		rebuildTree();
	}
		
	private void initUI() {
		setWindowTitle(tr("Staff Admin"));
			
		//
		// Widgets
		//
		staffTree = new QTreeWidget();
		staffTree.header().hide();
		
		// Active drag-and-drop moving of items in the tree
		/*
		staffTree.setDragEnabled(true);
		staffTree.setDropIndicatorShown(true);
		staffTree.viewport().setAcceptDrops(true);
		staffTree.setDragDropMode(QAbstractItemView.DragDropMode.InternalMove);
		*/
			
		staffTree.currentItemChanged.connect(
								this, "currentItemChanged(QTreeWidgetItem)");
		
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
		actionButtonsLayout.addStretch(1);
		actionButtonsLayout.addWidget(ownedCandidatesButton);
		actionButtonsLayout.addWidget(treeOwnedCandidatesButton);
		
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
		staffTree.clear();
		
		// Root item
		QTreeWidgetItem rootItem = new QTreeWidgetItem(staffTree, ROOT_TYPE);
		rootItem.setText(0, tr("Staff Members"));
		
		// Top-level items
		StaffHelper helper = new StaffHelper();
		
		try {
			RowSet members = helper.getTopLevelStaffMembers();
			
			for (Row member : members) {
				QTreeWidgetItem item = new QTreeWidgetItem(rootItem);
				
				item.setText(0, member.getString("Name"));
				item.setData(0, ID_ROLE, member.getInt("ID"));
				
				// Children
				doChildItem(member, item, helper);
				
				// Expand the entire tree
				staffTree.expandAll();
			}
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void doChildItem(Row parentRow, QTreeWidgetItem parentItem, StaffHelper helper) {
		//
		RowSet members = helper.getStaffMemberChildren(parentRow.getInt("ID"));
		
		for (Row member : members) {
			QTreeWidgetItem item = new QTreeWidgetItem(parentItem);
			
			item.setText(0, member.getString("Name"));
			item.setData(0, ID_ROLE, member.getInt("ID"));
			
			// Children
			doChildItem(member, item, helper);
		}
	}
	
	private void setUIEnabled(boolean enabled) {
		infoBox.setEnabled(enabled);
		
        removeButton.setEnabled(enabled);
        ownedCandidatesButton.setEnabled(enabled);
        treeOwnedCandidatesButton.setEnabled(enabled);
	}
	
	private void currentItemChanged(QTreeWidgetItem item) {
		if (item == null)
			return;
		
        if (item.type() == ROOT_TYPE) {
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
        		int id = Integer.parseInt(item.data(0, ID_ROLE).toString());
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
		QTreeWidgetItem item = staffTree.currentItem();
		
		if (item == null)
			return;
		
		StaffHelper helper = new StaffHelper();
    	
    	try {
    		int id = Integer.parseInt(item.data(0, ID_ROLE).toString());
    		Row member = new Row();
    		
    		member.put("Name", name.text());
    		member.put("Role", role.text());
    		member.put("RealInd", isReal.isChecked());
    		
    		helper.updateRecord(id, member);
    		
    		// Update the relevant item in the tree
    		item.setText(0, name.text());
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
		
		if (name == null || name.isEmpty())
			return;
		
		// Calculate parent id
		QTreeWidgetItem currentItem = staffTree.currentItem();
		Object parentId;
		
		if (currentItem == null || currentItem.type() == ROOT_TYPE)
			parentId = null;
		else
			parentId = currentItem.data(0, ID_ROLE);
			
		// Prepare the row
		Row row = new Row();
		row.put("Name", name);
		row.put("ParentID", parentId);
		
		// Into the DB
		StaffHelper helper = new StaffHelper();
		
		try {
    		helper.insertRecord(row);
    		
    		// Refresh tree
    		rebuildTree();
    		
    		// TODO: move to the new record
    	}
    	catch (Exception e) {
			MessageDialog.showException(this, e);
		}
    	finally {
    		helper.close();
    	}
	}
	
	private void removeStaffMember() {
		QTreeWidgetItem currentItem = staffTree.currentItem();
		    
		if (currentItem == null || currentItem.type() == ROOT_TYPE)
			return;
		
		int id = Integer.parseInt(currentItem.data(0, ID_ROLE).toString());
		String name = currentItem.text(0);
		
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
		QTreeWidgetItem currentItem = staffTree.currentItem();
		    
		if (currentItem == null || currentItem.type() == ROOT_TYPE)
			return;
		
		int id = Integer.parseInt(currentItem.data(0, ID_ROLE).toString());
		String name = currentItem.text(0);
		
		StaffHelper helper = new StaffHelper();
		
		try {
    		RowSet candidates = helper.getOwnedCandidates(id);
    		
    		// Create dialog and model
    		DataTableDialog dlg = new DataTableDialog(this);
    		
    		dlg.setWindowTitle(MessageFormat.format(tr("Candidates owned by {0}"), name));
    		dlg.resize((int) (dlg.width() * 1.25), dlg.height());
    		
    		RowSetModel model = new RowSetModel(RULER.split(","));
    		model.setData(candidates);
    		
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
		QTreeWidgetItem currentItem = staffTree.currentItem();
		    
		if (currentItem == null || currentItem.type() == ROOT_TYPE)
			return;
		
		int id = Integer.parseInt(currentItem.data(0, ID_ROLE).toString());
		String name = currentItem.text(0);
		
		StaffHelper helper = new StaffHelper();
		
		try {
    		RowSet candidates = helper.getTreeOwnedCandidates(id);
    		
    		// Create dialog and model
    		DataTableDialog dlg = new DataTableDialog(this);
    		
    		dlg.setWindowTitle(MessageFormat.format(tr("Candidates tree owned by {0}"), name));
    		dlg.resize((int) (dlg.width() * 1.25), dlg.height());
    		
    		RowSetModel model = new RowSetModel(TREE_RULER.split(","));
    		model.setData(candidates);
    		
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
}
