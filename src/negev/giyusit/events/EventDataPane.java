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
import negev.giyusit.util.Row;

import negev.giyusit.widgets.DialogField;

public class EventDataPane extends QWidget {
	
	// Widgets
	public QLabel id;
	public QLineEdit name;
	public QComboBox type;
	
	public QDateEdit startDate;
	public QDateEdit endDate;
	
	public QLineEdit location;
	public QLineEdit notes;
	public QComboBox owner;
	
	// Models
	private LookupTableModel eventTypesModel;
	private LookupTableModel staffModel;
	
	public EventDataPane() {
		initUI();
		initComboModels();
	}
	
	public Row toRow() {
		Row row = new Row();
		
		row.put("Name", name.text());
		row.put("StartDate", startDate.date());
		row.put("EndDate", endDate.date());
		row.put("Location", location.text());
		row.put("Notes", notes.text());
		
		row.put("TypeID", eventTypesModel.rowToKey(type.currentIndex()));
		row.put("OwnerID", staffModel.rowToKey(owner.currentIndex()));
		
		return row;
	}
	
	public void fromRow(Row row) {
		if (row == null)
			throw new NullPointerException("Null row");
		
		id.setText(row.getString("ID"));
		name.setText(row.getString("Name"));
		startDate.setDate(row.getDate("StartDate"));
		endDate.setDate(row.getDate("EndDate"));
		location.setText(row.getString("Location"));
		notes.setText(row.getString("Notes"));
		
		type.setCurrentIndex(
					eventTypesModel.keyToRow(row.getString("TypeID")));
			
		owner.setCurrentIndex(
					staffModel.keyToRow(row.getString("OwnerID")));
	}
	
	public boolean validateData() {
		if (name.text().isEmpty()) {
			MessageDialog.showUserError(window(), tr("Please enter a name for the event"));
			name.setFocus();
			return false;
		}
		if (type.currentIndex() == -1) {
			MessageDialog.showUserError(window(), tr("Please select a type for the event"));
			type.setFocus();
			return false;
		}
		if (startDate.date().daysTo(endDate.date()) < 0) {
			MessageDialog.showUserError(window(), tr("End date can't be before start date"));
			endDate.setFocus();
			return false;
		}
		return true;
	}
	
	private void initUI() {
		//
		// Widgets
		//
		id = new QLabel();
		
		name = new QLineEdit();
		
		type = new QComboBox();
		
		startDate = new QDateEdit();
		startDate.setCalendarPopup(true);
		startDate.setDisplayFormat("dd/MM/yyyy");
		startDate.setDate(QDate.currentDate());
		
		endDate = new QDateEdit();
		endDate.setCalendarPopup(true);
		endDate.setDisplayFormat("dd/MM/yyyy");
		endDate.setDate(QDate.currentDate());
		
		location = new QLineEdit();
		
		notes = new QLineEdit();
		
		owner = new QComboBox();
		
		//
		// Layout
		//
		QHBoxLayout topLayout = new QHBoxLayout();
		topLayout.addWidget(new DialogField(tr("ID: "), id));
		topLayout.addWidget(new DialogField(tr("<b>Name: </b>"), name), 2);
		topLayout.addWidget(new DialogField(tr("<b>Type: </b>"), type), 1);
		topLayout.addWidget(new DialogField(tr("Owner: "), owner), 1);
		
		QGroupBox datesBox = new QGroupBox();
		
		QFormLayout datesLayout = new QFormLayout(datesBox);
		datesLayout.addRow(tr("Start date: "), startDate);
		datesLayout.addRow(tr("End date: "), endDate);
		
		QGroupBox moreInfoBox = new QGroupBox();
		
		QFormLayout moreInfoLayout = new QFormLayout(moreInfoBox);
		moreInfoLayout.addRow(tr("Location: "), location);
		moreInfoLayout.addRow(tr("Notes: "), notes);
		
		QGridLayout mainLayout = new QGridLayout(this);
		mainLayout.setMargin(0);
		mainLayout.addLayout(topLayout,   1, 1, 1, 2);
		mainLayout.addWidget(datesBox,    2, 1);
		mainLayout.addWidget(moreInfoBox, 2, 2);
	}
	
	private void initComboModels() {
		eventTypesModel = new LookupTableModel("EventTypes");
		staffModel = new LookupTableModel("Staff");
		
		type.setModel(eventTypesModel);
		type.setModelColumn(LookupTableModel.VALUE_COLUMN);
		type.setCurrentIndex(-1);
		
		owner.setModel(staffModel);
		owner.setModelColumn(LookupTableModel.VALUE_COLUMN);
		owner.setCurrentIndex(-1);
	}
}
