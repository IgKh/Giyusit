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

import java.text.MessageFormat;

import negev.giyusit.db.LookupTableModel;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.widgets.DataGrid;
import negev.giyusit.widgets.DatePicker;
import negev.giyusit.widgets.DialogField;

public class CandidateStatusesDialog extends QDialog {

	// Widgets
	private DataGrid dataGrid;
	private QComboBox status;
	private QDateEdit startDate;
	
	// Buttons
	private QPushButton addStatusButton;
	private QPushButton removeLastStatusButton;
	
	// Properties
	private RowSetModel model;
	private LookupTableModel statusesModel;
	
	private int candidateId;
	private boolean dbModified = false;

	public CandidateStatusesDialog(QWidget parent, int candidateId) {
		super(parent);
		
		this.candidateId = candidateId;
		
		// Models
		model = new RowSetModel("StartDate,StatusName");
		statusesModel = new LookupTableModel("CandidateStatusValues", "ID", "Name", "EndDate isnull");
		
		DBValuesTranslator.translateModelHeaders(model);
		
		initUI();
		updateTitle();
		loadData();
	}
	
	public boolean isDbModified() {
		return dbModified;
	}
	
	private void initUI() {
		// Widgets
		dataGrid = new DataGrid();
		dataGrid.setModel(model);
		
		status = new QComboBox();
		
		status.setModel(statusesModel);
		status.setModelColumn(LookupTableModel.VALUE_COLUMN);
		status.setCurrentIndex(-1);
		
		status.currentIndexChanged.connect(this, "statusComboIndexChanged(int)");
		
		startDate = new DatePicker();
		
		addStatusButton = new QPushButton(tr("Update"));
		addStatusButton.setEnabled(false);
		addStatusButton.clicked.connect(this, "addStatus()");
		
		removeLastStatusButton = new QPushButton(tr("Remove Last Status"));
		removeLastStatusButton.clicked.connect(this, "removeLastStatus()");
		
		QPushButton closeButton = new QPushButton(tr("Close"));
		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
		closeButton.clicked.connect(this, "close()");
		
		// Layout
		QGroupBox updateStatusGroup = new QGroupBox(tr("Update Status"));
		
		QHBoxLayout updateStatusLayout = new QHBoxLayout(updateStatusGroup);
		updateStatusLayout.addWidget(new DialogField(tr("Status: "), status));
		updateStatusLayout.addWidget(new DialogField(tr("Start Date: "), startDate));
		updateStatusLayout.addStretch(1);
		updateStatusLayout.addWidget(addStatusButton);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(removeLastStatusButton);
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(updateStatusGroup);
		layout.addWidget(dataGrid, 1);
		layout.addLayout(buttonLayout);
	}
	
	private void loadData() {
		CandidateHelper helper = new CandidateHelper();
		
		try {
			model.setRowSet(helper.getCandidateStatuses(candidateId));
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void updateTitle() {
		CandidateHelper helper = new CandidateHelper();
		
		try {
			String title = tr("Statuses for candidate {0}");
			
			setWindowTitle(MessageFormat.format(title, 
									helper.candidateFullName(candidateId)));
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void statusComboIndexChanged(int index) {
		addStatusButton.setEnabled(index != -1);
	}
	
	private void addStatus() {
		//
		CandidateHelper helper = new CandidateHelper();
		
		try {
			if (helper.hasStatusInDate(candidateId, startDate.date())) {
				MessageDialog.showUserError(this,  
							tr("Candidate already has a status in the specified date"));
				
				return;
			}
			
			// Insert the new status
			int statusId = Integer.parseInt(
							statusesModel.rowToKey(status.currentIndex()));
			
			helper.addStatus(candidateId, statusId, startDate.date());
			
			dbModified = true;
			
			// Refresh window
			loadData();
			
			status.setCurrentIndex(-1);
			startDate.setDate(QDate.currentDate());
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
	
	private void removeLastStatus() {
		if (model.rowCount() <= 1) {
			MessageDialog.showUserError(this, tr("Can't remove initial status"));
			return;
		}
		
		CandidateHelper helper = new CandidateHelper();
		
		try {
			helper.removeLastStatus(candidateId);
			
			// Refresh window
			dbModified = true;
			loadData();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
}