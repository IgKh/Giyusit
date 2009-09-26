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

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import negev.giyusit.db.GenericDataView;
import negev.giyusit.util.RowSetModel;

public class CandidateDataView extends GenericDataView {
	
	private CandidateDialog itemDialog = null;
	
	public CandidateDataView(String name, String query, String ruler) {
		super(name, query, ruler);
	}
	
	public QIcon getIcon() {
		return new QIcon("classpath:/icons/candidates.png");
	}
	
	@Override
	public void viewDismissed() {
		super.viewDismissed();
		
		// Mark the cached item dialog for garbage collection when the user
		// leaves this data view
		itemDialog = null;
	}
	
	@Override
	public boolean showItemDialog(QWidget parent, QModelIndex index) {
		// Extract candidate id using the ID role
		int id = QVariant.toInt(index.data(RowSetModel.ID_ROLE));
		
		// Laziliy create dialog
		if(itemDialog == null)
			itemDialog = new CandidateDialog(parent);
		
		// Show dialog
		itemDialog.showCandidate(id);
		itemDialog.show();
		
		// Block until the dialog is closed
		while (itemDialog.isVisible())
			QApplication.processEvents();
		
		return itemDialog.isDbModified();
	}
}
