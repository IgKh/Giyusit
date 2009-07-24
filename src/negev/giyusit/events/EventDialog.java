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
package negev.giyusit.events;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.List;

import negev.giyusit.candidates.FindCandidatesDialog;
import negev.giyusit.db.LookupTableModel;
import negev.giyusit.util.BasicRow;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.GenericItemDialog;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.util.RowSet;
import negev.giyusit.util.Row;
import negev.giyusit.DataTable;
import negev.giyusit.DataView;

public class EventDialog extends QDialog {
	
	// Widgets
	private EventDataPane eventData;
	private DataTable dataTable;
	
	private QCheckBox activeAttendantsOnly;
	
	// Buttons
	private QPushButton saveButton;
	private QPushButton resetButton;
	
	private QPushButton exportDataButton;
	private QPushButton addCandidatesButton;
	private QPushButton deleteCandidateButton;
	
	private QPushButton closeButton;
	
	// Properties
	private int eventId;
	private boolean dbModified = false;
	
	private EventDialogDataView dataView;
	
	public EventDialog(QWidget parent, int eventId) {
		super(parent);
		
		this.eventId = eventId;
		
		dataView = new EventDialogDataView(eventId);
		
		initUI();
		
		loadEventDetails();
		loadAttendants();
	}
	
	public boolean isDbModified() {
		return dbModified;
	}
	
	private void initUI() {
		//
		// Widgets
		//
		eventData = new EventDataPane();
		
		dataTable = new DataTable();
		dataTable.setFilterEnabled(false);
		dataTable.setStatusEnabled(false);
		dataTable.setDataView(dataView);
		
		dataTable.selectionModel().selectionChanged.connect(this, "selectionChanged()");
		dataTable.internalView().activated.connect(this, "updateCandidate(QModelIndex)");
		
		activeAttendantsOnly = new QCheckBox(tr("Show active attendants only"));
		activeAttendantsOnly.toggled.connect(this, "loadAttendants()");
		
		saveButton = new QPushButton(tr("Save"));
		saveButton.setIcon(new QIcon("classpath:/icons/save.png"));
		saveButton.clicked.connect(this, "saveEventDetails()");
		
		resetButton = new QPushButton(tr("Reset"));
		resetButton.setIcon(new QIcon("classpath:/icons/revert.png"));
		resetButton.clicked.connect(this, "loadEventDetails()");
		
		exportDataButton = new QPushButton(tr("Export Data..."));
		exportDataButton.setIcon(new QIcon("classpath:/icons/export.png"));
		exportDataButton.clicked.connect(dataTable, "exportData()");
		
		addCandidatesButton = new QPushButton(tr("Add Candidates..."));
		addCandidatesButton.clicked.connect(this, "addCandidates()");
		
		deleteCandidateButton = new QPushButton(tr("Delete Candidate"));
		deleteCandidateButton.setEnabled(false);
		deleteCandidateButton.clicked.connect(this, "deleteCandidate()");
		
		closeButton = new QPushButton(tr("Close"));
		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
		closeButton.clicked.connect(this, "close()");
		
		//
		// Layout
		//
		QGroupBox eventDetailsBox = new QGroupBox(tr("Event Details"));
		
		QHBoxLayout eventDetailsButtonsLayout = new QHBoxLayout();
		eventDetailsButtonsLayout.addStretch(1);
		eventDetailsButtonsLayout.addWidget(saveButton);
		eventDetailsButtonsLayout.addWidget(resetButton);
		
		QVBoxLayout eventDetailsLayout = new QVBoxLayout(eventDetailsBox);
		eventDetailsLayout.addWidget(eventData);
		eventDetailsLayout.addLayout(eventDetailsButtonsLayout);
		
		QGroupBox eventAttendanceBox = new QGroupBox(tr("Event Attendance"));
		
		QHBoxLayout eventAttendanceButtonsLayout = new QHBoxLayout();
		eventAttendanceButtonsLayout.addWidget(exportDataButton);
		eventAttendanceButtonsLayout.addStretch(1);
		eventAttendanceButtonsLayout.addWidget(addCandidatesButton);
		eventAttendanceButtonsLayout.addWidget(deleteCandidateButton);
		
		QVBoxLayout eventAttendanceLayout = new QVBoxLayout(eventAttendanceBox);
		eventAttendanceLayout.addWidget(activeAttendantsOnly);
		eventAttendanceLayout.addWidget(dataTable);
		eventAttendanceLayout.addLayout(eventAttendanceButtonsLayout);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(eventDetailsBox);
		layout.addWidget(eventAttendanceBox, 1);
		layout.addLayout(buttonLayout);
	}
	
	private void selectionChanged() {
		deleteCandidateButton.setEnabled(dataTable.selectionModel().hasSelection());
	}
	
	private void updateWindowTitle() {
		setWindowTitle(eventData.name.text());
	}
	
	private void loadEventDetails() {
		EventHelper helper = new EventHelper();
		
		try {
			eventData.fromRow(helper.fetchById(eventId));
			
			updateWindowTitle();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void saveEventDetails() {
		// Validate
		if (!eventData.validateData())
			return;
		
		// Into the DB
		EventHelper helper = new EventHelper();
		
		try {
			helper.updateRecord(eventId, eventData.toRow());
			
			updateWindowTitle();
			dbModified = true;
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void loadAttendants() {
		EventHelper helper = new EventHelper();
		
		try {
			if (activeAttendantsOnly.isChecked())
				dataView.setData(helper.getActiveEventAttendants(eventId));
			else
				dataView.setData(helper.getAllEventAttendants(eventId));
						
			dataTable.refresh();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void addCandidates() {
		// Show candidate selection dialog
		FindCandidatesDialog dlg = new FindCandidatesDialog(this, 
										FindCandidatesDialog.Mode.MultiSelect);
		
		dlg.exec();
		
		// Add the selected candidates
		EventHelper helper = new EventHelper();
		
		try {
			int[] selectedCandidates = dlg.selectedCandidates();
			
			for (int candidateId : selectedCandidates) {
				helper.addEventAttendance(eventId, candidateId);
			}
			
			loadAttendants();
			dbModified = true;
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void deleteCandidate() {
		// Get the selected candidate
		List<QModelIndex> selection = dataTable.selectionModel().selectedRows();
		
		if (selection.isEmpty() || selection.get(0) == null)
			return;
		
		QModelIndex index = selection.get(0);
		int candidateId = Integer.parseInt(index.data().toString());
		
		// Remove this attendance
		EventHelper helper = new EventHelper();
		
		try {
			helper.deleteEventAttendance(eventId, candidateId);
			
			dataTable.selectionModel().clearSelection();
			
			loadAttendants();
			dbModified = true;
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void updateCandidate(QModelIndex index) {
		if (index == null)
			return;
		
		// Extract candidate ID
		int candidateId = Integer.parseInt(index.model().data(index.row(), 0).toString());
		
		// Show dialog
		AttendanceItemDialog dlg = new AttendanceItemDialog(this);
		EventHelper helper = new EventHelper();
		
		try {
			dlg.fromRow(helper.getAttendanceRow(eventId, candidateId));
				
			if (dlg.exec() == QDialog.DialogCode.Rejected.value())
				return;
			
			// Update
			helper.updateAttendanceRow(eventId, candidateId, dlg.toRow());
			
			loadAttendants();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
}

/**
 * A special internal data view that allow modification in the item dialog
 */
class EventDialogDataView extends DataView {
	
	private RowSetModel model;
	
	public EventDialogDataView(int eventId) {
		model = new RowSetModel(new String[] {"ID", "FirstName", "LastName", 
												"Gender", "Address", "City", 
												"ZipCode", "EMail", "AttType", "Notes"});
		
		DBValuesTranslator.translateModelHeaders(model);
	}
	
	public void setData(RowSet rowSet) {
		model.setData(rowSet);
	}
	
	@Override
	public String getName() {
		return "";
	}
	
	@Override
	public QAbstractItemModel getModel() {
		return model;
	}
}

/**
 * A dialog for modifying attendance items
 */
class AttendanceItemDialog extends GenericItemDialog {
	
	private QComboBox attendanceType;
	private QLineEdit notes;
	
	private LookupTableModel lookupModel;
	
	public AttendanceItemDialog(QWidget parent) {
		super(parent);
		
		lookupModel = new LookupTableModel("AttendanceTypes");
		
		setWindowTitle(tr("Giyusit"));
		
		// Widgets
		attendanceType = new QComboBox();
		attendanceType.setModel(lookupModel);
		attendanceType.setModelColumn(LookupTableModel.VALUE_COLUMN);
		attendanceType.setCurrentIndex(-1);
		
		notes = new QLineEdit();
		
		// Layout
		addField(tr("Attendance Type: "), attendanceType);
		addField(tr("Notes: "), notes);
	}
	
	@Override
	public Row toRow() {
		Row row = new BasicRow();
		
		row.put("Notes", notes.text());
		row.put("AttTypeID", lookupModel.rowToKey(attendanceType.currentIndex()));
		
		return row;
	}
	
	@Override
	public void fromRow(Row row) {
		if (row == null)
			throw new NullPointerException("Null row");
		
		notes.setText(row.getString("Notes"));
		attendanceType.setCurrentIndex(lookupModel.keyToRow(row.getString("AttTypeID")));
	}

	@Override
	public boolean validate() {
		return true;
	}
}
