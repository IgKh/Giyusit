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

import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.Row;
import negev.giyusit.widgets.DialogField;

/**
 * A widget for selecting a single candidate by ID or search parameters
 */
public class CandidatePickerWidget extends QWidget {
	
	public Signal1<Integer> candidateSelected = new Signal1<Integer>();
	
	// Widgets
	private QLineEdit id;
	private QLabel name;
	
	// Buttons
	private QPushButton loadDataButton;
	private QPushButton findCandidatesButton;
	
	// Properties
	private int selectedCandidate = -1;
	
	public CandidatePickerWidget(QWidget parent) {
		initUI();
	}
	
	/**
	 * Returns the ID of the candidate selected by this picker, or -1 if none
	 * yet.
	 */
	public int getSelectedCandidate() {
		return selectedCandidate;
	}
	
	private void initUI() {
		//
		// Widgets
		//
		id = new QLineEdit();		
		id.setMaximumWidth(50);
		id.setValidator(new QIntValidator(this));
		id.returnPressed.connect(this, "loadData()");
		
		name = new QLabel();
		
		loadDataButton = new QPushButton(tr("Load Data"));
		loadDataButton.clicked.connect(this, "loadData()");
		
		findCandidatesButton = new QPushButton(tr("Find Candidates..."));
		findCandidatesButton.clicked.connect(this, "findCandidates()");
		
		//
		// Layout
		//
		QHBoxLayout layout = new QHBoxLayout(this);
		layout.setMargin(0);
		layout.addWidget(new DialogField(tr("Candidate ID: "), id));
		layout.addWidget(loadDataButton);
		layout.addWidget(findCandidatesButton);
		layout.addWidget(new DialogField(tr("Name: "), name), 1);
	}
	
	private void loadData() {
		if (id.text().isEmpty())
			return;
		
		tryCandidate(Integer.parseInt(id.text())); 
	}
	
	private void findCandidates() {
		FindCandidatesDialog dlg = new FindCandidatesDialog(this, 
											FindCandidatesDialog.Mode.SingleSelect);
		
		int result = dlg.exec();
		if (result == QDialog.DialogCode.Rejected.value())
			return;
		
		int candidateId = dlg.selectedCandidates()[0];
		
		id.setText(String.valueOf(candidateId));
		tryCandidate(candidateId); 
	}
	
	// Common to loadData() and findCandidates()
	private void tryCandidate(int candidateId) {
		CandidateHelper helper = new CandidateHelper();
		
		try {
			Row candidate = helper.fetchById(candidateId);
			
			if (candidate == null) {
				MessageDialog.showInformation(window(), 
										tr("Candidate doesn't exist"));
				return;
			}
			
			name.setText(candidate.getString("FirstName") + 
									" " + candidate.getString("LastName"));
			
			selectedCandidate = candidateId;
			candidateSelected.emit(candidateId);
		}
		catch (Exception e) {
			MessageDialog.showException(window(), e);
		}
		finally {
			helper.close();
		}
	}
}
