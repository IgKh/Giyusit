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

import com.trolltech.qt.gui.*;

import negev.giyusit.db.LookupTableModel;
import negev.giyusit.util.GenericItemDialog;
import negev.giyusit.util.row.BasicRow;
import negev.giyusit.util.row.Row;

/**
 * A dialog for modifying attendance items
 */
public class EventAttendanceItemDialog extends GenericItemDialog {

	private QComboBox attendanceType;
	private QLineEdit notes;
	
	private LookupTableModel lookupModel;
	
	public EventAttendanceItemDialog(QWidget parent) {
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
	public void fromRow(Row row) {
		if (row == null)
			throw new NullPointerException("Null row");
		
		notes.setText(row.getString("Notes"));
		attendanceType.setCurrentIndex(lookupModel.keyToRow(row.getString("AttTypeID")));
	}

	@Override
	public Row toRow() {
		Row row = new BasicRow();
		
		row.put("Notes", notes.text());
		row.put("AttTypeID", lookupModel.rowToKey(attendanceType.currentIndex()));
		
		return row;
	}

	@Override
	public boolean validate() {
		return true;
	}
}