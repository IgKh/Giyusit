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

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.List;

import negev.giyusit.candidates.FindCandidatesDialog;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.DataTable;

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
	
	private RowSetModel model;
	
	public EventDialog(QWidget parent, int eventId) {
		super(parent);
		
		this.eventId = eventId;
		
		model = new RowSetModel("ID*,FirstName,LastName,Gender,Address,City," + 
								"ZipCode,EMail,AttType,Notes");
		
		DBValuesTranslator.translateModelHeaders(model);
		
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
		dataTable.setModel(model);
		
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
				model.setRowSet(helper.getActiveEventAttendants(eventId));
			else
				model.setRowSet(helper.getAllEventAttendants(eventId));
						
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
		int candidateId = QVariant.toInt(index.data(RowSetModel.ID_ROLE));
		
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
		int candidateId = QVariant.toInt(index.data(RowSetModel.ID_ROLE));
		
		// Show dialog
		EventAttendanceItemDialog dlg = new EventAttendanceItemDialog(this);
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