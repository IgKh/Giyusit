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

import com.trolltech.qt.gui.*;

import negev.giyusit.util.MessageDialog;

public class DeleteCandidateDialog extends QDialog {
	
	// Widgets
	private CandidatePickerWidget candidatePicker;
	
	// Buttons
	private QPushButton deleteButton;
	private QPushButton cancelButton;
	
	public DeleteCandidateDialog(QWidget parent) {
		super(parent);
		
		initUI();
	}
	
	private void initUI() {
		setWindowTitle(tr("Delete Candidate"));
		
		//
		// Widgets
		//
		candidatePicker = new CandidatePickerWidget(null);
		candidatePicker.candidateSelected.connect(this, "candidateSelected()");
		
		String warning = tr("<b>Warning!</b> This operation is irreversible. " + 
							"Deleting a candidate will pernametly remove it<br> " + 
							"and all associated information!");
		
		deleteButton = new QPushButton(tr("Delete"));
		deleteButton.setEnabled(false);
		deleteButton.clicked.connect(this, "doDelete()");
		
		cancelButton = new QPushButton(tr("Cancel"));
		cancelButton.clicked.connect(this, "reject()");
		
		//
		// Layout
		//
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(deleteButton);
		buttonLayout.addWidget(cancelButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(candidatePicker);
		layout.addSpacing(8);
		layout.addWidget(new QLabel(warning));
		layout.addLayout(buttonLayout);
	}
	
	private void candidateSelected() {
		deleteButton.setEnabled(true);
	}
	
	private void doDelete() {
		CandidateHelper helper = new CandidateHelper();
		
		try {
			int id = candidatePicker.getSelectedCandidate();
			
			helper.deleteRecord(id);
			
			// Notify user
			MessageDialog.showSuccess(this, tr("Candidate deleted successfuly"));
			
			accept();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
}
