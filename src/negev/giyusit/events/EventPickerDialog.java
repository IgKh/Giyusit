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

import negev.giyusit.db.LookupTableModel;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.row.Row;
import negev.giyusit.widgets.DialogField;

public class EventPickerDialog extends QDialog {
	
	// Widgets
	private QComboBox events;
	private QLabel type;
	private QLabel startDate;
	
	// Buttons
	private QPushButton okButton;
	
	// Fields
	private LookupTableModel eventsModel;
	private LookupTableModel eventTypesModel;
	private int selectedEventId = -1;
	
	public EventPickerDialog(QWidget parent) {
		super(parent);
		
		initUI();
		
		// Setup combo
		eventsModel = new LookupTableModel("Events", "ID", "Name");
		
		events.setModel(eventsModel);
		events.setModelColumn(LookupTableModel.VALUE_COLUMN);
		events.setCurrentIndex(-1);
		events.currentIndexChanged.connect(this, "currentEventChanged(int)");
		
		//
		eventTypesModel = new LookupTableModel("EventTypes");
	}
	
	public int getSelectedEventID() {
		return selectedEventId;
	}
	
	private void initUI() {
		setWindowTitle(tr("Add to Event"));
		
		//
		// Widgets
		//
		events = new QComboBox();
		
		type = new QLabel();
		startDate = new QLabel();
		
		okButton = new QPushButton(tr("OK"));
		okButton.setEnabled(false);
		okButton.clicked.connect(this, "accept()");
		
		QPushButton cancelButton = new QPushButton(tr("Cancel"));
		cancelButton.clicked.connect(this, "reject()");
				
		//
		// Layout
		//
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
				
		QGridLayout layout = new QGridLayout(this);
		layout.addWidget(new DialogField(tr("<b>Event:</b> "), events), 1, 1, 1, 2);
		layout.addWidget(new DialogField(tr("<b>Type: </b>"), type), 2, 1);
		layout.addWidget(new DialogField(tr("<b>Start Date: </b>"), startDate), 2, 2);
		layout.setRowMinimumHeight(3, 5);
		layout.addLayout(buttonLayout, 4, 1, 1, 2);
	}
	
	private void currentEventChanged(int index) {
		if (index == -1) {
			okButton.setEnabled(false);
			return;
		}
		
		okButton.setEnabled(true);
		
		// Show event details
		EventHelper helper = new EventHelper();
		
		try {
			int id = Integer.parseInt(eventsModel.rowToKey(index));
			selectedEventId = id;
			
			Row eventRow = helper.fetchById(id);
			
			// Type
			String typeId = eventRow.getString("TypeID");
			type.setText(eventTypesModel.data(
					eventTypesModel.keyToRow(typeId), 
					LookupTableModel.VALUE_COLUMN).toString());
			
			// Date
			QDate date = eventRow.getDate("StartDate");
			startDate.setText(date.toString("dd/MM/yyyy"));
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
}